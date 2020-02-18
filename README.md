**JAVA Basketball GM**

This program aims to simulate a basketball league such as the NBA. You can pick make teams draft players, and have those
teams simulate games against each other. 

This program requires com.googlecode.clichemaven and com.googlecode.json-simple to work. They are added in the pom.xml
as dependencies. Please try to use Maven to import this project and it's dependencies. Many IDEs do this automatically.

**GUI Based Operation (Recommended)**

*NOTE: TO Run the GUI, simple run the main method in the class application/JavaBasketballGMGUI.*

This program now includes a GUI controller that allows you the view your team and simulate games graphically. The GUI
starts up with simple screen asking if you would like to load a previous league or start a new one.

If you select a new league, it will populate an initial pool of free agents and teams. You will be asked to pick a team 
from the list. At this point, all teams are empty. You will be given an option to either automate the entire draft process
or draft yourself. I recommend using the manual draft since it allows you to build the team to your needs.

In the manual draft page, the teams will take turns picking players from the pool of free agents. Once concluded, you are
brought to the Main View. The Main view dynamically refreshes based on what the user wants to do.

On the left, the user will see his or her teams roster, and will be able to click on the names to view details about
each individual player on their team. To the right, there is a table listing your teams current games. If there are no games,
one can click the "Schedule More Games" button. To simulate a game, double-click the game in the table and the 
center view will update to show the game view, where you can see a log of what is happening in the game. Once the game
is over, a game stat box is shown. You may click the row of any previous games in the table to view the box score for that game

On the bottom, buttons are shown for all of the other teams in the league. Clicking these will bring you to a "Team Info" 
page where you can inspect the other teams in the league. 

To quit, simply press the "Quit" button. You will be given a prompt to save your league.


**Command Line Based Operation**

NOTE: The CLI-based controller is not being updated anymore, since the GUI based controller is much better. Please
note running thr CLI-based controller will only run limited functionality of the program now.

The program can also be interfaced through a shell system. It uses Cliche shell to help aide with this. 

To start the program, run the main method found in src/main/java/application/Main. A shell will launch.

Type `?list` to list the vaid commands in the league:

```JavaBasketballGM
   cmd> ?list
   abbrev	name	params
   ll	load-league	(filePath)
   sl	save-league	()
   nl	new-league	()
   pad	perform-automated-draft	()
   srr	setup-round-robin	()
   siroro	simulate-round-robin	()
   lt	list-teams	()```

