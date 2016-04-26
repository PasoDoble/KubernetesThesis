#First goal: read in the data from each file of tallied, baselined data

#File format: price,tally

files <- list.files(path="e:\\GitHub\ Repositories\\Thesis-Repository\\workspace\\AWSAnalysisTool\\HistoryThesisLevelGraphs\\", pattern="*.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  #file <- read.table(files[i],header=TRUE, sep=";", stringsAsFactors=FALSE)
  file <- read.table(files[i], header=F, sep=",", col.names=c("Availability","Price"))
  
  filename <- basename(files[i])
  #print(filename)
  numServers <- substr(filename,1,nchar(filename)-4)
  print(numServers)
  dir = "e:\\GitHub\ Repositories\\Thesis-Repository\\workspace\\AWSAnalysisTool\\HistoryThesisLevelGraphs"
  
  newsubfile <- paste(dir,numServers,sep="\\")
  # print(newsubfile)
  
  newfile <- paste(newsubfile,"pdf",sep=".")
  #print(newfile)
  
  
  pdf(newfile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  plot(Price ~ Availability, data=file,pch=19,cex=0.1)
  abline(lm(file$Price ~ file$Availability), col="red") # regression line (y~x) 
  #lines(file$percentage,lw1$fitted,col="blue",lwd=3)
  #plot(file$price,file$percentage)
  #lo <- loess(file$percentage~file$price)
  #lines(predict(lo), col="red", lwd=2)
  #lines(lowess(file$price,file$percentage), col="red")
  title(paste("Availability of a system for ",numServers,sep=" "))
  
  dev.off()
  
  
  #one file at a  time
  #break
}

print("Job Complete")
