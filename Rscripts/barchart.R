require(reshape2)
require(ggplot2)
library(ggthemes)
library(ggpubr)

args = commandArgs(trailingOnly=TRUE)

data = read.csv(args[1], header=FALSE)
names(data) <- c('Condition','Count')


p=ggbarplot(
  data, x = "Condition", y = "Count", 
  add = c("mean_se", "jitter"),
  fill = "Condition",
  palette = c("#4fd882", "#2e9dd4"),
  ylab="Normalised read counts"
) + stat_compare_means(method="t.test", comparisons=list(unique(data["Condition"])))

if(!is.null(args[3])){
  p=p+ylab(args[3])
}

print(args[4])
print(args[5])

if(!is.null(args[4])){
  p=p+ylim(as.numeric(args[4]), as.numeric(args[5]))
}


pdf(args[2])
print(p)
dev.off()