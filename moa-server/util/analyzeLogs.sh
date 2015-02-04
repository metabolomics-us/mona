#a simple script, which parses the mona log files and generates pretty R reports for us
#to make sense of all the data

#please ensure that this is your pattern layout for all the files
#
# '%t %-5c{1} %d{HH:mm:ss} %m%n'
#
#please provide me with the logfile directory location to the mona import log


rm -f *.pdf
rm -f *.png

###
# import log section
##

echo "thread;time;id;inchi;duration;transactions;flushes;statements" > temp.import.log

cat $1/monaImport.log | grep --text SpectraUploadJob |grep --text 'took'  | sed s/,//g | awk '{print $1 ";" $3 ";" $8 ";" $10 ";" $13 ";" $16 ";" $19 ";" $23}' 1>> temp.import.log


###
# validation log section
##

echo "thread;time;id;duration" > temp.validation.log

cat $1/monaSpectraValidation.log  |grep --text 'took'  | sed s/,//g | awk '{print $1 ";" $3 ";" $8 ";" $11}' 1>> temp.validation.log


###
# memory consumption section
##

echo "time;total;free;used" > temp.memory.log

cat $1/monaMemory.log | grep --text MemoryConsumptionJob | grep --text usage | awk '{print $3 ";" $9 ";" $7 ";" $11 }' 1>> temp.memory.log


###
# session flushing section
##

echo "time;total;free;used" > temp.flush.memory.log

cat $1/monaFlush.log  | grep --text FlushSessionJob | grep --text 'after flushing' | awk '{print $3 ";" $11 ";" $9 ";" $13 }' 1>> temp.flush.memory.log

echo "time;duration" > temp.flush.duration.log

cat $1/monaFlush.log   | grep --text FlushSessionJob | grep --text 'flushed session in' | awk '{print $3 ";" $7/1000}' 1>> temp.flush.duration.log


##
# generating R reports based on our provided files
##

Rscript generateReport.r
