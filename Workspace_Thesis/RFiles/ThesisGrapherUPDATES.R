#First goal: read in the data from each file of tallied, baselined data

#File format: price,tally

files <- list.files(path="e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryThesisLevelGraphs\\", pattern="*Availability.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  file <- read.table(files[i], header=F, sep=",", col.names=c("Availability","Price"))
  
  filename <- basename(files[i])
  #print(filename)
  numServers <- substr(filename,1,nchar(filename)-4)
  print(numServers)
  numberOfServers <- substr(numServers,1,nchar(numServers)-19)
  print(numberOfServers)
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
  xlimitlevel <- 100
  lowerlimit <- 0
  
  xrange <- range(file$Availability)
  yrange <- range(file$Price)
  
  print(ylimitlevel)
  
  pdf(newfile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  #plot(Price ~ Availability, type="l",data=file,pch=19,cex=0.1,col="red",ylim=c(0, ylimitlevel))#ylim=c(0, 13))
  plot(xrange, yrange, type="n",xlab="Availability (%)",ylab="Price ($/hour)",xaxs="i",yaxs="i",ylim=c(0, ylimitlevel),xlim=c(90,100))
  lines(file$Availability,file$Price,type="b",lwd=1.5,pch=19,col="red")
  
  lines(file$Availability,horizontalfile$Price,col="blue",lty=2)
  
  
  
  #title(paste("Availability of a system for",numServers,sep=" "))
  
  dev.off()
  
  excessfilename <- paste(numServers,".txt",sep="Excess")
  excesssubfilename <- paste(numServers,"Excess",sep="")
  
  excessFullDirectory <- paste(dir,excessfilename,sep="\\")
  excessSubFullDirectory <- paste(dir,excesssubfilename,sep="\\")
  
  if(file.exists(excessFullDirectory)){
    excessfile <- read.table(excessFullDirectory, header=F, sep=",", col.names=c("Percentage","Servers"))
    
    newexcessfile <- paste(excessSubFullDirectory,"pdf",sep=".")
    pdf(newexcessfile)
    
    newxrange <- excessfile$Percentage
    offset <- strtoi(numberOfServers, base=0L)
    newyrange <- excessfile$Servers-offset
    newylimit <- head(newyrange,1)#+ceiling(.1*newyrange[0])
    newylimit <- newylimit+ceiling(.2*newylimit)
    print(newylimit)
    
    plot(newxrange, newyrange, type="n",xlab="Availability (%)",ylab="Excess Servers Available",xaxs="i",yaxs="i",ylim=c(0, newylimit),xlim=c(0,100))
    lines(newxrange,newyrange,type="b",lwd=1.5,pch=19,col="green")
    #lines(excessfile$Percentage,excessfile$Servers,type="b",lwd=1.5,pch=19,col="green")
    #plot(Servers ~ Percentage, type="l",data=excessfile,pch=19,cex=0.1,col="green")
    dev.off()
    
  }

  #one file at a  time
  #break
}

print("Job Complete")
