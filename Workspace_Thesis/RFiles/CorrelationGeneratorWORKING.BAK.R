#First goal: read in the data from each file of tallied, baselined data

#File format: price,tally

files <- list.files(path="e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\TimeMapping\\BidLevel_$0.02\\", pattern="*.txt", full.names=T, recursive=FALSE)
for(i in 1:length(files)){
  file <- read.table(files[i], header=F, sep=",", col.names=c("TimeIndex","AvailableData"))
  
  filename <- basename(files[i])
  #print(filename)
  instanceType <- substr(filename,1,nchar(filename)-4)
  print(instanceType)
  dir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\TimeMapping\\BidLevel_$0.02"
  newDir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\InterAvailabilities\\BidLevel_$0.02"
  baseDir <- "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool"
  
  newsubfile <- paste(newDir,instanceType,sep="\\")
  # print(newsubfile)
  

  corDataFile <- paste(newDir,filename,sep="\\")
  file.create(corDataFile)

  orderedfilename <- "InstanceOrderedID.txt"
  orderedFullFile <- paste(baseDir,orderedfilename,sep="\\")
  

  #print("Pre-If Statement")
  if(file.exists(orderedFullFile)){
    orderingfile <- read.table(orderedFullFile, header=F, sep=",", col.names=c("Instances"))
    print("Begin Correlation Calculations")
    line <- instanceType
    for(j in orderingfile$Instances){
      #print(j)
      compFile <- paste(j,"txt",sep=".")
      compFullFile <- paste(dir,compFile,sep="\\")
      #print(compFullFile)
      if(file.exists(compFullFile)){
        compFileTable <- read.table(compFullFile, header=F, sep=",", col.names=c("TimeIndex","AvailableData"))
        
        vec1 <- as.numeric(file$AvailableData)
        vec2 <- as.numeric(compFileTable$AvailableData)
        
        correlation <- cor(vec1, vec2)
        line <- paste(line, correlation, sep=",")
        write(correlation,file=corDataFile,append=TRUE)
        #print(correlation)
      }
    }
    #print(line)
    #write(line,file=corDataFile,append=TRUE)
    
  }
  
  
  
  graphFile <- paste(newsubfile,"pdf",sep=".")
  
  ylimitlow <- -1
  ylimithigh <- 1
  
  pdf(graphFile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  #plot(Price ~ Availability, type="l",data=file,pch=19,cex=0.1,col="red",ylim=c(0, ylimitlevel))#ylim=c(0, 13))
  #lines(file$Availability,horizontalfile$Price,col="blue")
  
  
  
  title(paste("Correlation of Instance Types for",instanceType,sep=" "))
  
  dev.off()
  

  
  
  
  #one file at a  time
  #break
}

print("Job Complete")
