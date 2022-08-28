require(reshape2)
require(ggplot2)
library(ggthemes)


args = commandArgs(trailingOnly=TRUE)

data = read.csv(args[1], header=FALSE)
names(data) <- c('Peptide','Run','Intensity')


p = ggplot(data = data, aes(x = Run, y = Intensity, group = Peptide, colour=Peptide))+geom_line()




pdf(args[2])
print(p)
dev.off()