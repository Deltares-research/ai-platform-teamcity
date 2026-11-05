# AI Platform TeamCity settings

The manual **Build and test AI Platform SDK** configuration checks out
`ai-platform-mlops` main, builds a wheel and source distribution, and runs the
SDK tests against the installed wheel. It uses the SDK's locked runtime
dependencies and a separate pytest installation. Successful builds retain the
distributions as TeamCity artifacts; they do not publish packages.

The private source repository uses a TeamCity secure-value reference in the
settings. Its GitHub credential is stored on the TeamCity server, not here.
Changes to build settings or scripts can expose credentials, so repository
write access and permission to run builds must be restricted to trusted users.
