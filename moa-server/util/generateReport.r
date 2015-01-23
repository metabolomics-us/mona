library(gplots)
library(png)
library(gridExtra)


"disable scientific numbers"
options(scipen=999)

"read our prepared data files"
importDuration = read.table("temp.import.log",header=TRUE,sep = ";")
validationDuration = read.table("temp.validation.log",header=TRUE,sep = ";")



memoryConsumption = read.table("temp.memory.log",header=TRUE,sep = ";")
flushMemoryConsumption = read.table("temp.flush.memory.log",header=TRUE,sep = ";")
flushDuration = read.table("temp.flush.duration.log",header=TRUE,sep = ";")


"function to help us generate plots"
generateHistogram <- function(object,title="Histogram",xlab="x",breaks=75){
    hist(object,breaks=breaks,col="lightblue",main=title,xlab=xlab)
}

generateLineChart <- function(object,title="Histogram",xlab="x",ylab="y",type="l"){
    plot(object, col="lightblue",xlab=xlab, ylab=ylab, main=title,type=type)
}

"convert time to seconds"
toms = function(time) {
    sapply(strsplit(as.character(time), ':', fixed = T),
         function(x) sum(as.numeric(x)*c(3600000,60000,1000)))

}

"configures out png object"
setPngOption <- function(file){

    png(    file,
            width     = 3.25,
            height    = 3.25,
            units     = "in",
            res       = 320,
            pointsize = 4
    )
}



"long running calculations"

jobSubmissionTime <- sapply(importDuration$time,toms)

for(i in 1:length(jobSubmissionTime)-1){
    jobSubmissionTime[i] = jobSubmissionTime[i+1] - jobSubmissionTime[i]
}

"chop the last one off since it's not a difference anyway"
jobSubmissionTime = head(jobSubmissionTime,-1)

"LETS START WITH PLOTS"

"General import related plots"


setPngOption("graph-1.png")
generateHistogram(importDuration$duration,"Import Duration","time in s")

dev.off()

setPngOption("graph-2.png")
generateLineChart(importDuration$duration,"Import time over time","spectra count","time in s")

dev.off()

"memory consumption plots"


setPngOption("graph-3.png")
plot(0,ylim=c(min(0),max(memoryConsumption$total/1024/1024)),xlim=(c(0,length(memoryConsumption$total))),xlab="uptime in minutes",ylab="consumptions in MB",main="Memory consumption over time")

lines(memoryConsumption$used/1024/1024,col="red")
lines(memoryConsumption$free/1024/1024,col="green")
lines(memoryConsumption$total/1024/1024,col="blue")

legend(x="topleft",legend=c('used','free','totally available'),col=c(1,2,3),lty=c(1,1,1))

dev.off()

"render our performance table"

setPngOption("graph-4.png")

medianSpectraPerSecond = median(importDuration$duration)
meanSpectraPerSecond = mean(importDuration$duration)

importPerformance <- data.frame(

    Timeframe = c('Ms/s','Ms/min','Ms/h','MS/d'),
    Median = c(1/medianSpectraPerSecond,60/medianSpectraPerSecond,60*60/medianSpectraPerSecond,60*60*24/medianSpectraPerSecond),
    Mean = c(1/meanSpectraPerSecond,60/meanSpectraPerSecond,60*60/meanSpectraPerSecond,60*60*24/meanSpectraPerSecond)
)


textplot(format(importPerformance,digits=1),valign="top")
title("Estimates how many spectra in a given time the system can import")

"our output file"
dev.off()


"generate a chart off the job execution rate over time"

setPngOption("graph-5.png")

plot(head(importDuration$time,-1),jobSubmissionTime,xlab="submission time",ylab="duration to previous job in ms",main="Job Submission Time over current runtime",col="lightblue")

dev.off()

"histogram of the job submission time"

setPngOption("graph-6.png")

generateHistogram(jobSubmissionTime,"Job submission distribution","time in ms")

dev.off()

"calculate job submission statistics"

setPngOption("graph-7.png")

medianJobsPerSecond = median(jobSubmissionTime)/1000
meanJobsPerSecond = mean(jobSubmissionTime)/1000


jobPerformance <- data.frame(

    Timeframe = c('Ms/s','Ms/min','Ms/h','MS/d'),
    Median = c(1/medianJobsPerSecond,60/medianJobsPerSecond,60*60/medianJobsPerSecond,60*60*24/medianJobsPerSecond),
    Mean = c(1/meanJobsPerSecond,60/meanJobsPerSecond,60*60/meanJobsPerSecond,60*60*24/meanJobsPerSecond)
)


textplot(format(jobPerformance,digits=1),valign="top")
title("Estimates how many Jobs in a given time the system can handle")

dev.off()

"calculate on how many samples this analysis was based"

setPngOption("graph-8.png")

textplot(format(
 data.frame(

     'Spectra' = c(length(importDuration$time))
 )
,digits=1),valign="top")
title("Amount of data used for these statistics")



dev.off()


"General validation plots"


setPngOption("graph-9.png")
generateHistogram(validationDuration$duration,"Validation Duration of Spectra","time in s")

dev.off()

setPngOption("graph-10.png")

generateLineChart(validationDuration$duration,"Validation time over time","spectra count","time in s")

dev.off()


"our output file"

"combine all to one pdf"

pdf("MonaRuntimeReport.pdf")

"first page"

grid.arrange(
    rasterGrob(readPNG("graph-1.png", native = FALSE),interpolate = FALSE),
    rasterGrob(readPNG("graph-2.png", native = FALSE),interpolate = FALSE),
    rasterGrob(readPNG("graph-3.png", native = FALSE),interpolate = FALSE),
    rasterGrob(readPNG("graph-4.png", native = FALSE),interpolate = FALSE),
    rasterGrob(readPNG("graph-5.png", native = FALSE),interpolate = FALSE),
     rasterGrob(readPNG("graph-6.png", native = FALSE),interpolate = FALSE),

ncol = 2)

"second page"

grid.arrange(
     rasterGrob(readPNG("graph-7.png", native = FALSE),interpolate = FALSE),
     rasterGrob(readPNG("graph-8.png", native = FALSE),interpolate = FALSE),
     rasterGrob(readPNG("graph-9.png", native = FALSE),interpolate = FALSE),
     rasterGrob(readPNG("graph-10.png", native = FALSE),interpolate = FALSE),
     
     
     ncol = 2)



dev.off()