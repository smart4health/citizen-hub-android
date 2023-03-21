# Citizen Hub Android

## Building

### Requirements

#### GitHub Access

In order to resolve dependencies published to GitHub, you need a GitHub account and you must generate a personal access token with the proper access.

Gradle must be able to find the properties `github.username` and `github.token` with the necessary credential information (for instance, in `~/.gradle/gradle.properties` with Gradle default configuration).

```
github.username=<username>
github.toke=<token>
```

#### Environment Configuration

A file named `env.<flavor>.properties` must exist in the project root for each of the build flavors (`development` and `stable`) containing the endpoint configuration.
 
These files are key value pairs separated by `=`.

The following properties must be defined. 

- `smart4HealthId`

    A `string` representing the client id attributed to the application by the Smart4Health project.

- `smart4HealthSecret`

    A `string` representing the client secret for the indicated `clientId`.

- `smart4HealthRedirectScheme`

    A `string` representing the Smart4Health endpoint key identification.

- `smart4HealthEnvironment`

    A `string` representing the type of environment used in Smart4Health.

- `smart4HealthDebug`

    A `boolean` indicating if Smart4Health debug should be active.
 
- `smart4HealthPlatform`

    A `string` representing the Smart4Health platform logging in to.

- `smart4HealthAppUrl`

    A `string` for the Smart4Health app URL.

- `smartBearApiKey`

    A `string` specifying the Smart Bear API key attributed to the application by the Smart Bear project.

- `smartBearUrl`

    A `string` specifying the Smart Bear URL endpoint.

