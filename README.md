This solution reads all the logs zipped, unzipped in a directory, and execute them on the rules written in rules.xml and suggests the actions needed to be taken by Support folks to the client who has reported the Issue.
Actions are also mentioned in the rules.xml.
Engineering Team will be delievering the 2 files:
1. JAR file. Which is the actual Solution.
2. rules.xml. Which contains all the rules.
Attached is the rules.xml in the root location of this solution.

Steps to run the solution:
1. Put the logs in a location. Say: D://CS-12345
2. Put the jar and xml file in one location, say D://solution
3. Run the solution using this command from location of the solution:
java -jar SOLUTION.jar D://CS-12345 2019-Jan-19 Sat 02:30:37.549 30000

Arguments in above command:
This part "java -jar SOLUTION.jar" runs the jar file and add arguments.
This part "2019-Jan-19 Sat 02:30:37.549 " is the timestamp where we want solution to search for. We will be knowing in advance an appropriate timestamp to search for solution within all the logs.
This part "30000" is milliseconds width. This means solution will search only logs which are 30000 milliseconds away from given time stamp.
Milliseconds can be skipped also and the command can look like this "java -jar SOLUTION.jar D://CS-12345 2019-Jan-19 Sat 02:30:37.549".
In above case default width will be taken which is mentioned in application.properties as range = 10000

This solution opens up the END POINTS here:
1. The port can be changed in application.properties file with key value as server.port = 8759
2. localhost:8759/logs, gives all the logs.
3. localhost:8759/logs/rules, gives all the rules.
4. localhost:8759/logs/execute, it excutes all the rules and gives all rules that passed with their mentioned actions.

How to write the rules:
Have a look at the rules.xml.
It starts with tag "rules", which have many rules as tag "rule".
"rule" contains "name", "description", conditions, query and actions.
conditions have many condition tags mentioned with appropriate name.
A condition have filter attributes on logs like classname, level etc, like:
                        <className>org.apache.ignite.internal.IgniteKernal</className>
		<level>INFO</level>
			<message>
				<token>hosts=</token>
				<regex>[0-9]*</regex>
				<token>nodes=</token>
				<regex>[0-9]*</regex>
				<token>CPUs=</token>
				<regex>[0-9]*</regex>
			</message>

tag query will have the query composing all the conditions mentioned in conditions tag.
                                   
Please take the sample folder in the root of this repository.
Solution contains SOLUTION.jar and rules.xml file.
CS-12345 contains the logs.
run it as :
java -jar SOLUTION.jar D:\Sample\CS-12345 2019-Jan-19 Sat 02:30:37.549 30000
