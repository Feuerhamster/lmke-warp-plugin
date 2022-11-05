# LMKE Warps Spigot Plugin
**Game Version:** 1.15

## Features
- Private warps
- Public warps (poi's = points of interest)
- Highly customizable
- Translations (german, english)
- Economy support with Vault
- Teleport signs (for poi's)
- Limit how many warps and poi
- Input validation for warp/poi names

### Private warps
Private warps are per player and only the player who created it can teleport to it.
It's useful for example to set homes.

### Public warps
Public warps, or poi's (points of interest) are global and everyone can teleport to them.
It's useful for example if a player has a shop and want other players to visit their shop.

This type of warps can have **teleport signs**. Simply create a sign and write `[poi]` in the first line and the name of the poi in the second line.

## Commands

| command               | description                                             |
|-----------------------|---------------------------------------------------------|
| `/warp`               | General command. Prints help if no argument is provided |
| `/warp <name>`        | Teleport to a warp point                                |
| `/warp help`          | Print help                                              |
| `/warp list`          | List all your warp points                               |
| `/warp create <name>` | Create a warp point                                     |
| `/warp delete <name>` | Delete a warp point                                     |


| command              | description                                             |
|----------------------|---------------------------------------------------------|
| `/poi`               | General command. Prints help if no argument is provided |
| `/poi <name>`        | Teleport to a point of interest                         |
| `/poi help`          | Print help                                              |
| `/poi list`          | List all points of interest that you created            |
| `/poi create <name>` | Create a point of interest                              |
| `/poi delete <name>` | Delete a point of interest                              |

## Permissions

- `lmke-warps.warps` Help page and teleport to warp
- `lmke-warps.warps.list`
- `lmke-warps.warps.create`
- `lmke-warps.warps.delete`

- `lmke-warps.poi` Help page and teleport to poi
- `lmke-warps.poi.list`
- `lmke-warps.poi.create`
- `lmke-warps.poi.delete`
- `lmke-warps.admin` Delete poi's that aren't yours

- `lmke-warps.bypass.economy`
- `lmke-warps.bypass.limit`

- `lmke-warps.sign.create`
- `lmke-warps.sign.use`