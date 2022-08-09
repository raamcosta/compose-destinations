```mermaid
flowchart LR
root([Root]) -- start --- greeting
root([Root]) --- settings([Settings]) -- start --- settings_main
                 settings([Settings]) --- settings_profile
                   

classDef navgraph fill:#f96;
class root,settings navgraph;
```
