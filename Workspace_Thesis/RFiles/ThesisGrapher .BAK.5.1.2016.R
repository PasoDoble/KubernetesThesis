#First goal: read in the data from each file of tallied, baselined data

#File format: price,tally

files <- list.files(path="e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryThesisLevelGraphs\\", pattern="*Availability.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  #file <- read.table(files[i],header=TRUE, sep=";", stringsAsFactors=FALSE)
  file <- read.table(files[i], header=F, sep=",", col.names=c("Availability","Price"))
  
  filename <- basename(files[i])
  #print(filename)
  numServers <- substr(filename,1,nchar(filename)-4)
  print(numServers)
  dir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryThesisLevelGraphs"
  
  newsubfile <- paste(dir,numServers,sep="\\")
  # print(newsubfile)
  
  newfile <- paste(newsubfile,"pdf",sep=".")
  #print(newfile)
  
  comparisonfile <- paste(numServers,".txt",sep="OD")
  comparisonFullDirectory <- paste(dir,comparisonfile,sep="\\")
  horizontalfile <- read.table(comparisonFullDirectory, header=F, sep=",", col.names=c("Availability","Price"))
  
  #totalofondemand <- sum(horizontalfile$Price)
  #totalnumentries <- nrow(horizontalfile$Price)
  #ondemandprice <- totalofondemand/totalnumentries
  #ylimitlevel <- ondemandprice+1
  ylimitlevel <- tail(horizontalfile$Price, n=1)
  ylimitlevel <- ylimitlevel+.5
  
  print(ylimitlevel)
  
  pdf(newfile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  plot(Price ~ Availability, type="l",data=file,pch=19,cex=0.1,col="red",ylim=c(0, ylimitlevel))#ylim=c(0, 13))
  lines(file$Availability,horizontalfile$Price,col="blue")
  #abline(lm(file$Price ~ file$Availability), col="red") # regression line (y~x)
 # par(new=TRUE)
  #plot(horizontalfile$Availability,horizontalfile$Price,type="l",col="blue")
  #plot(Price ~ Availability, data=horizontalfile,pch=19,cex=0.1)
  #abline(lm(horizontalfile$Price ~ horizontalfile$Availability), col="blue") # regression line (y~x)
  
  #lines(horizontalfile$Availability,horizontalfile$Price,col="blue")
  
  
  title(paste("Availability of a system for",numServers,sep=" "))
  
  dev.off()
  
  #one file at a  time
  #break
}

print("Job Complete")
