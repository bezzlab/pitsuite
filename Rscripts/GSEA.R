
library(clusterProfiler)
library(enrichplot)
library(ggplot2)

args = commandArgs(trailingOnly=TRUE)


organism = "org.Hs.eg.db"
library(organism, character.only = TRUE)

rnaOrProtein = args[2]
if(rnaOrProtein=="rna"){
  fileName <- 'Rscripts/gseaGenesRna.txt'
}else{
  fileName <- 'Rscripts/gseaGenesProteins.txt'
}

genes = readChar(fileName, file.info(fileName)$size)
genes = strsplit(genes, ",")[[1]]

if(args[1]=="kegg"){
  genes = mapIds(org.Hs.eg.db, genes, 'ENTREZID', 'SYMBOL')
}


gene_list = sort(genes, decreasing = TRUE)

if(args[1]=="go"){
  res <- enrichGO(gene_list,
                  OrgDb         = org.Hs.eg.db,
                  ont           = "BP",
                  pAdjustMethod = "BH",
                  keyType="SYMBOL",
                  pvalueCutoff  = 0.05,
                  qvalueCutoff  = 0.05)
}else{
  res <- enrichKEGG(gene         = gene_list,
                   organism     = "hsa",
                   pvalueCutoff = 0.05)
}


if(rnaOrProtein=="rna"){
  jpeg(filename="plots/gseaRna.jpeg", width = as.numeric(args[3]), height = as.numeric(args[4]))
}else{
  jpeg(filename="plots/gseaProteins.jpeg", width = as.numeric(args[3]), height = as.numeric(args[4]))
}

plt = dotplot(res, showCategory=15, font.size=14) + theme(plot.background = element_rect(fill = "#F4F4F4"), panel.background = element_rect(fill = "#F4F4F4", ),
                                                          panel.grid.major = element_line(size = 0.5, linetype = 'solid',
                                                                                          colour = "white"), 
                                                          panel.grid.minor = element_line(size = 0.25, linetype = 'solid',
                                                                                          colour = "white"),
                                                          legend.background = element_rect(fill="#F4F4F4"),
                                                          legend.key = element_rect(fill = "#F4F4F4"))
plot(plt)
dev.off()
#ggsave("plots/go.png", plot = plt, width=as.numeric(args[2]), height = as.numeric(args[3]))




