#Changing things up a bit


dir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\TimeMapping\\BidLevel_$0.02"
newDir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\InterAvailabilities\\BidLevel_$0.02"
baseDir <- "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool"

orderedfilename <- "InstanceOrderedID.txt"
orderedFullFile <- paste(baseDir,orderedfilename,sep="\\")


#print("Pre-If Statement")
if(file.exists(orderedFullFile)){
  
  orderingfile <- read.table(orderedFullFile, header=F, sep=",", col.names=c("Instances"))
  print("Begin Correlation Calculations")
  
  correlationVector <- vector('double')
  
  for(i in orderingfile$Instances){
    
    
    theFile <- paste(i,"txt",sep=".")
    theFullFile <- paste(dir,theFile,sep="\\")
    if(file.exists(theFullFile)){
      file <- read.table(theFullFile, header=F, sep=",", col.names=c("TimeIndex","AvailableData"))
      
      filename <- basename(theFullFile)
      #print(filename)
      instanceType <- i
      print(instanceType)
      
      
      newsubfile <- paste(newDir,"CorrelationHeatMap",sep="\\")
      # print(newsubfile)
      
      
      corDataFile <- paste(newDir,filename,sep="\\")
      file.create(corDataFile)
      
      orderedfilename <- "InstanceOrderedID.txt"
      orderedFullFile <- paste(baseDir,orderedfilename,sep="\\")
      
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
          if(is.na(correlation)){
            correlation <- 0
          }
          correlationVector <- c(correlationVector,correlation)
          #line <- paste(line, correlation, sep=",")
          write(correlation,file=corDataFile,append=TRUE)
          #print(correlation)
        }
      }
      #print(line)
      #write(line,file=corDataFile,append=TRUE)
      
      
      
      
      
      
      #one file at a  time
      #break
    }
  }
  
  #(carArray <- array(
  #  correlationVector,
  #  dim = c(29, 29),
  #  dimnames = list(orderingfile$Instances,orderingfile$Instances)
  #))
  
  graphFile <- paste(newsubfile,"pdf",sep=".")
  
  ylimitlow <- -1
  ylimithigh <- 1
  
  pdf(graphFile)
  #lw1 = loess(file$percentage ~ file$price,data=file)
  #plot(Price ~ Availability, type="l",data=file,pch=19,cex=0.1,col="red",ylim=c(0, ylimitlevel))#ylim=c(0, 13))
  #lines(file$Availability,horizontalfile$Price,col="blue")
  
  
  corMatrix <- matrix(correlationVector, nrow = 29, ncol = 29, 
                      dimnames = list(orderingfile$Instances,orderingfile$Instances))
  
  #corheatmap <- heatmap(corMatrix, Rowv=NA, Colv=NA, cellnote = corMatrix, notecol = "black", 
  #                      col = cm.colors(256), scale="column", margins=c(5,10))
  
  corheatmap <- heatmap(corMatrix, Rowv=NA, Colv=NA, cellnote = corMatrix, 
                        notecol = "black", col = grey(seq(1,0,-0.01)), scale="column", margins=c(5,10))
  
  
  #title(paste("Correlation of Instance Types for",instanceType,sep=" "))
  
  dev.off()

}

print("Job Complete")
