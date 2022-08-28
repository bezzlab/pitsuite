# script to generate a heatmap for the top
# most different genes. Uses normalized read counts
# to generate the heatmap and dge to select top genes

## three arguments are passed:
# 1 - path to the PNG plot, to be saved
# 2 - path to the normalized read count csv
# 3 - path to the dge

## check if gplots is installed
if ("gplots" %in% rownames(installed.packages())){
    require("gplots")
} else {
    install.packages("gplots")
    require("gplots")
}

# get the arguments passed
args = commandArgs(trailingOnly=TRUE)

## get the arguments
plotPath  <-  args[1]
normalizedReadCountCsv  <- args[2]
dgeCsvs  <-  args[3]


## read the data
normalizedReadCount <- read.csv(normalizedReadCountCsv)
dge  <-  read.csv(dgeCsvs)


# get sorted gene symbols
dgeSymbolCol <-  match("x", tolower(names(dge)))
dgeSortedSymbol  <-  dge[
        with(dge, order(-abs(log2FoldChange), padj )),
        ][, dgeSymbolCol]


# get col index for the gene names ("x") and the values
normalizedReadCountColNames  <-  tolower(names(normalizedReadCount))
normalizedReadCountGeneNamesCol  <-  match("x", normalizedReadCountColNames)
normalizedReadCountValueCols  <-  1:dim(normalizedReadCount)[2]
normalizedReadCountValueCols  <-  normalizedReadCountValueCols[-normalizedReadCountGeneNamesCol]


# add data into a matrix
topN <- 50
topDiffGenesMatix <- matrix(nrow=topN, ncol = length(normalizedReadCountValueCols))

colnames(topDiffGenesMatix)  <-  names(normalizedReadCount)[normalizedReadCountValueCols]
rownames(topDiffGenesMatix)  <-  rep("", topN)


# extract the values
prevGenes  <-  NA
count  <-  0
i  <-  1
while( count < topN && i <= length(dgeSortedSymbol)){
    geneName <- toString(dgeSortedSymbol[i])
    rowIndex  <-  match(geneName, normalizedReadCount[,normalizedReadCountGeneNamesCol])
    if (!is.na(rowIndex) && !(geneName %in% prevGenes)){
        count  <-  count + 1
        prevGenes[count]  <- geneName
        rowValues <- normalizedReadCount[rowIndex,normalizedReadCountValueCols]
        rownames(topDiffGenesMatix)[count]  <-  geneName
        topDiffGenesMatix[count,]  <- as.numeric(rowValues) # the rest
    }
    i  <-  i+1
}



# heatmap plot
png(file=plotPath, height = 1500, width=(1500 * 0.8411552))
par(oma = c(2,2,2,16))
heatmap.2(topDiffGenesMatix, col=bluered,  scale="row",  cexRow = 2.3, cexCol=2, trace="none")
dev.off()
