import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

version = "2026.2"

project {
    vcsRoot(SdkSource)
    buildType(SdkBuild)
}

object SdkSource : GitVcsRoot({
    name = "AI Platform MLOps"
    url = "https://github.com/Deltares-research/ai-platform-mlops.git"
    branch = "refs/heads/main"
    authMethod = password {
        userName = "x-access-token"
        password = "credentialsJSON:3a6edeb0-d48b-4cca-a5ad-1d0be834a49f"
    }
})

object SdkBuild : BuildType({
    name = "Build and test AI Platform SDK"
    description = "Builds SDK distributions from main and tests the installed wheel."
    artifactRules = """
        sdk-dist/%teamcity.build.id%/*.whl => sdk
        sdk-dist/%teamcity.build.id%/*.tar.gz => sdk
    """.trimIndent()

    vcs {
        root(SdkSource)
        root(DslContext.settingsRoot, "+:.teamcity => .teamcity-ci")
    }

    steps {
        script {
            name = "Build and test SDK"
            scriptContent = """
                set -eu
                python -m pip install --disable-pip-version-check uv==0.12.10
                bash .teamcity-ci/sdk-build.sh "%system.teamcity.build.tempDir%" "%teamcity.build.id%"
            """.trimIndent()
            dockerImage = "containers.deltares.nl/docker-proxy/python:3.12-slim"
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "DOCKER_REGISTRY_HARBOR"
            }
        }
        xmlReport {
            reportType = XmlReport.XmlReportType.JUNIT
            rules = "sdk-dist/%teamcity.build.id%/test-results.xml"
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.name", "Linux")
        equals("docker.server.osType", "linux")
    }
})
