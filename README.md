This utility serves JSON based end point to all logs present in a folder and all sub directories.

Features:
1. Simple JSON based filter search.
2. De compress gz and zip files on the fly.
3. Show results in readable format, separated by different log files.

Instructions:
1. Run Spring Boot from LogAnalyzerMain by providing the path to the logs as first argument to the application. 

Application will open end points at port : 8759

All logs can be accessed at :
1. localhost:8759/logs

Filtered logs can be accessed here:
localhost:8759/logs/filter
With below JSON in post:
{
            "starting": "2018-Aug-02 Thu 00:00:00.483",
            "ending": "2018-Aug-14 Tue 00:00:24.482",
            "level": "INFO",
            "message": "aa"
}

Above will display all logs filtered by timestamp in between starting and ending, level and stack trace which contains json message.
