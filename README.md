**JAVA Basketball GM**

This program aims to simulate a basketball league such as the NBA. You can pick make teams draft players, and have those
teams simulate games against each other. 

This program requires com.googlecode.clichemaven and com.googlecode.json-simple to work. They are added in the pom.xml
as dependencies. Please try to use Maven to import this project and it's dependencies. Many IDEs do this automatically.

As of now this program can be interfaced through a shell system. It uses Cliche shell to help aide with this. 

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

