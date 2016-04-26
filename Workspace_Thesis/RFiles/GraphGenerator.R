#First goal: read in the data from each file of tallied, baselined data

#File format: price,tally

files <- list.files(path="e:\\GitHub\ Repositories\\Thesis-Repository\\workspace\\AWSAnalysisTool\\HistoryDataTalliedBaselinedCosts\\", pattern="*.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  #file <- read.table(files[i],header=TRUE, sep=";", stringsAsFactors=FALSE)
  file <- read.table(files[i], header=F, sep=",", col.names=c("price", "occurences"))
  
  filename <- basename(files[i])
  #print(filename)
  instancetype <- substr(filename,1,nchar(filename)-4)
  print(instancetype)
  
  dir = "e:\\GitHub\ Repositories\\Thesis-Repository\\workspace\\AWSAnalysisTool\\HistoryDataTBGrpahs"
  newsubfile <- paste(dir,instancetype,sep="\\")
 # print(newsubfile)
  
  newfile <- paste(newsubfile,"pdf",sep=".")
  #print(newfile)
  
  
  pdf(newfile)
  lw1 = loess(file$occurences ~ file$price,data=file)
  plot(file$occurences ~ file$price, data=file,pch=19,cex=0.1)
  lines(file$occurences,lw1$fitted,col="blue",lwd=3)
  #plot(file$price,file$occurences)
  #lo <- loess(file$occurences~file$price)
  #lines(predict(lo), col="red", lwd=2)
  #lines(lowess(file$price,file$occurences), col="red")
  title(paste("PDF of spot price history",instancetype,sep=" "))
  
  dev.off()
  #break
}


#lapply(files, function(x) {
  #t <- read.table(x, header=F, sep=",", col.names=c("price", "occurences")) # load file
  #print("pre-read table")
  #t <- read.table(x, header=F)
  #print("post-read table")
  
  
  
  #MUST I USE THE ATTACH COMMAND HERE?
  #plot(price, occurences)
  #abline(lm(occurences~price))
  #filename <- basename(x)
  #titleofgraph <- paste("Probability Density Function for", filename, " ")
  #title(titleofgraph)
  
  #newfilename <- paste(filename, "pdf", ".")
  #pdf(newfilename)
  
  # apply function
  #out <- function(t)
  # write to file
  # write.table(out, "path/to/output", sep="\t", quote=F, row.names=F, col.names=T)
  
 # print(out)
  
#})
print("Job Complete")
