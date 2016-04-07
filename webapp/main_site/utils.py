#helper functions
from datetime import datetime

log_file = "logs.txt"
time_format = '%m/%d/%Y %H:%M:%S'

#writes to the log file called log.txt located in the main directory
def write_log(text): 
    file = open(log_file,'a')
    file.write("[" + datetime.now().strftime(time_format) + "] " + text + "\n")
    file.close()
    return

def clear_log():
    open(log_file,'w').close()
    return
