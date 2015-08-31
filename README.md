# Parse HipChat JSON to wiki #
This little project:

1. Reads the files in a HipChat export (must be unzipped, delete rooms that should not be included)
2. Creates wiki markup formatted tables with the contents
    * Deletes empty messages, welcome messages and messages from JIRA
    * Replaces @name-references with [~name] -- assumes names in HipChat and Confluence are the same
    * Replaces /code with {noformat}...{noformat}
3. Writes one .room-file for each room, to be inserted as wiki markup in Confluence
    * The result format is

        ```
        h2. <room name>
        
        | date | @user | text |
        | <blank if equal to above> | <ditto> | ... |
        ```

    * The text tables are split at each 1000 rows, to stop Confluence from choking...
    
### Structure ###

The project is written in Scala using Akka, because I can :)

Communication outline:
<img src="hipchatparse.png" />

## Usage ##

1. sbt assembly
2. java -jar &lt;....&gt;/hipchatparse.jar baseDir [resultDir]

## Authors ##
lre = Lars Reed, Mesan AS

## Notes ##

### History ###
* v1 2015.08.31 lre Initial version

### Caveats ###
* Overwrites the result files without warning

### TODO ###
yes...  e.g. 

* Akka-level tests...
* alternative @-mentions
