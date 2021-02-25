# Ollclans (1.16.5)
This is a Spigot plugin for version 1.16.5!

Ollclans is a plugin which allows players to create their own clans, track its stats and invite new members to join them on their adventures!
This exciting and new plugin also allows players to teleport to their specified clan home/flag point to return to their base and continuously upgrade their base whilst fighting alongside their other clan mates. 
Players will feel conntected more then ever before as they can easily create their own clan or join others using the custom commands listed below. 

Please raise any issues you spot or have whilst using this plugin.

## Available commands:
- /clan : Main clan command (displays all available cmds, dependant on clan status)
- /clan < leaderboard > : Displays all created clans, its member count and its invite only status/disbanded status.
- /clan < create > : Clan create command, takes arguments of <clanname> and <clantag>. Clantag will display color codes!
- /clan < leave > : Leave a clan command but only if player is not a leader of a clan and actually in a clan.
- /clan < join > : Join the specified clan, if its invite only status is set to Open.
- /clan < home > : Visit the clan's home/flag point.
- /clan < members > : Displays all clan members in players clan, if player is in a clan.
- /clan < stats > : Display player's current clan's stats.
- /clan < inviteonly > : Toggle your owned clan's invite only status between being Open or Invite-Only. This is displayed in the leaderboard command. Open will allow players to join, however Invite-Only only allows players to be invited.
- /clan < disband > : Disband your owned clan, if the player is the leader of a clan. This also set's the leaderboard to display that the clan has been disbanded, sets the member count to 0 and removes the owner.
- /clan < setflag > : Set your clan's home/flag point. This generates a randomly coloured flag next to the home point and displays a name-tag that is set to the clan's name above the home/flag point.

## Methods used:
- SetUpAndSendChat(Player player, AsyncChatEvent event)
  - This method formats the player's message to include the player's clan (if they are in one) alongside any other plugin's formatting. For example: "<ClanTag> [ADMIN] yllo : test message!"
- SetUpClanSpawns()
  - This method will loop through all created clans (but only if there has been atleast 1 created) and place a new armour stand with a name-tag of the clan's name.
- UpdateClanHashMap()
  - Every single time a player uses the leaderboard command, it will refresh the hashmap which is stored for the leaderboard with the most recent values. This allows the plugin to format the leaderboard to contain specific clan data & paginate the pages if there are more than 5 clans.

## Examples:
This section will detail examples for the commands above, and what the configuration file COULD look like once in use.

Example config file:
```
prefix:
- SVR
settings:
  clans-protected: false
  clans-showtag: true
  clans-tagfirstchar: '<'
  clans-taglastchar: '>'
  clans-defaultinviteonlystatus: 0
  banned-tags:
  - admin
  - moderator
  - helper
  - Swear1
  - SwearWordExample2
clans:
  Example01:
    clantag: NUM1
    owner: 'null'
    level: 1
    balance: 300
    invite-only: 2
    disbanded: true
    members: {}
    flag:
      flagSet: false
      flagX: 1
      flagY: 1
      flagZ: 1
  Example02:
    clantag: NUM2
    owner: yllo
    level: 1
    balance: 300
    invite-only: 1
    disbanded: false
    members: 
    - ExampleUser1
    - ExampleUser2
    flag:
      flagSet: true
      flagWorld: world
      flagX: -95
      flagY: 73
      flagZ: -264
  Example03:
    clantag: NUM3
    owner: Example4
    level: 1
    balance: 300
    invite-only: 1
    disbanded: false
    members: {}
    flag:
      flagSet: false
      flagX: 1
      flagY: 1
      flagZ: 1
```


### Settings
#### This section contains all of the essential settings for ollclans to run.
  - Prefix is the prefix used in the messages sent to the player when using /clan commands. For example:
    - [SVR] You cannot use this command!
    - [SVR] /clan create < name > < tag > 
  - clans-protected is currently unused, however is in place for future development.
  - clans-showtag is a simple boolean which determines whether the clantag is shown or not.
  - clans-tagfirstchar and clans-taglastchar is the characters which wrap around the clantag.
  - clans-defaultinviteonlystatus:
    - 0: Open for players joining.
    - 1: Invite Only, players cannot /clan join 
  - banned-tags can contain any words that you do not want as a clan tag. This will also disable players from using ANY of these words as their clan name!


### Clans
#### This section contains explainations for the example clans above.
- Example01 is an example of a disbanded clan. As you can see, the owner is set to null and members list is cleared, the invite only status is the set to 2 and the disbanded status is set to true.
- Example02 is an example of a clan with two members and its flag set. Once a clan has set its flag, the flagSet stat is set to true and the home/flag position is set. This then allows players to visit their /clan home. Currently, the only way for a player to reset their flag is if an admin changes their clan's flagSet stat to false again.
- Example03 is an example of a clan without any members and without its flag set. If you are using the example config and want to add yourself into this example clan, please remove the braces, place a line break and a hyphen below with your name and then /reload the server.
