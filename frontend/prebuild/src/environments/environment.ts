// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const environment = {
  production: false,
  API_URL_AUTH: 'http://localhost:8082',
  API_URL_GAME_ROUND: 'http://localhost:8080/round',
  API_URL_GAME_WS: 'ws://localhost:8080/round/ws',
  API_URL_PLAYERS: 'http://localhost:8081/player',
  API_URL_RANKS: 'http://localhost:8081/rank'
};
