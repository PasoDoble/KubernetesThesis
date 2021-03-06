#First goal: read in the data from each file of tallied, baselined data

#File format: price,tally

files <- list.files(path="e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryDataCDFBaselined\\", pattern="*.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  #file <- read.table(files[i],header=TRUE, sep=";", stringsAsFactors=FALSE)
  file <- read.table(files[i], header=F, sep=",", col.names=c("price", "percentage"))
  
  filename <- basename(files[i])
  #print(filename)
  instancetype <- substr(filename,1,nchar(filename)-4)
  print(instancetype)
  dir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryDataCDFBaselined"
  
  newsubfile <- paste(dir,instancetype,sep="\\")
 # print(newsubfile)
  
  newfile <- paste(newsubfile,"pdf",sep=".")
  #print(newfile)
  
  
  pdf(newfile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  plot(file$percentage ~ file$price, data=file,pch=19,cex=0.1)
  #lines(file$percentage,lw1$fitted,col="blue",lwd=3)
  #plot(file$price,file$percentage)
  #lo <- loess(file$percentage~file$price)
  #lines(predict(lo), col="red", lwd=2)
  #lines(lowess(file$price,file$percentage), col="red")
  title(paste("CDF of spot price history",instancetype,sep=" "))
  
  dev.off()
  
  
  #one file at a  time
  #break
}

#For absolute CDFs
files <- list.files(path="e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryDataCDF\\", pattern="*.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  #file <- read.table(files[i],header=TRUE, sep=";", stringsAsFactors=FALSE)
  file <- read.table(files[i], header=F, sep=",", col.names=c("price", "percentage"))
  
  filename <- basename(files[i])
  #print(filename)
  instancetype <- substr(filename,1,nchar(filename)-4)
  print(instancetype)
  dir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\HistoryDataCDF"
  
  newsubfile <- paste(dir,instancetype,sep="\\")
  # print(newsubfile)
  
  newfile <- paste(newsubfile,"pdf",sep=".")
  #print(newfile)
  
  
  pdf(newfile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  plot(file$percentage ~ file$price, data=file,pch=19,cex=0.1)
  #lines(file$percentage,lw1$fitted,col="blue",lwd=3)
  #plot(file$price,file$percentage)
  #lo <- loess(file$percentage~file$price)
  #lines(predict(lo), col="red", lwd=2)
  #lines(lowess(file$price,file$percentage), col="red")
  title(paste("CDF of spot price history",instancetype,sep=" "))
  
  dev.off()
  
  
  #one file at a  time
  #break
}

print("Job Complete")
