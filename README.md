This utility serves JSON based end point to all logs present in a folder and all dub directories.

Instructions:
1. Run Spring Boot from LogAnalyzerMain by providing the path to the logs as first argument to the application. 

Application will open end points at port : 8759

All logs can be accessed at :
1. localhost:8759/logs

Filtered logs can be accessed here:
localhost:8759/logs/filter
With below JSON in post:
{
            "starting": 1533148200483,
            "ending": 1537085024482
}

