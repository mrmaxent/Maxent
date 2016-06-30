maxnet.default.regularization <-
function(p, m)
{
   isproduct <- function(x) grepl(":", x) & !grepl("\\(", x)
   isquadratic <- function(x) grepl("^I\\(.*\\^2\\)", x)
   ishinge <- function(x) grepl("^hinge\\(", x)
   isthreshold <- function(x) grepl("^thresholds\\(", x)
   iscategorical <- function(x) grepl("^categorical\\(", x)
   regtable <- function(name, default) {
      if (ishinge(name)) return(list(c(0,1), c(0.5,0.5)))
      if (iscategorical(name)) return(list(c(0,10,17), c(0.65, 0.5, 0.25)))
      if (isthreshold(name)) return(list(c(0,100), c(2.0, 1.0)))
      default
   }
   lregtable <- list(c(0,10,30,100), c(1,1,0.2,0.05))
   qregtable <- list(c(0,10,17,30,100), c(1.3,0.8,0.5,0.25,0.05))
   pregtable <- list(c(0,10,17,30,100), c(2.6,1.6,0.9,0.55,0.05))
   mm <- m[p==1,]
   np <- nrow(mm)
   lqpreg <- lregtable
   if (sum(isquadratic(colnames(mm)))) lqpreg <- qregtable
   if (sum(isproduct(colnames(mm)))) lqpreg <- pregtable
   classregularization <- sapply(colnames(mm), function(n) {
      t <- regtable(n, lqpreg)
      approx(t[[1]], t[[2]], np, rule=2)$y
   }) / sqrt(np)
   # increase regularization for extreme hinges
   ishinge <- grepl("^hinge\\(", colnames(mm))
   hmindev <- sapply(1:ncol(mm), function(i) {
      if (!ishinge[i]) return(0)
      avg <- mean(mm[,i])
      std <- max(sd(mm[,i]), 1/sqrt(np))
      std*.5/sqrt(np)
   })
   # increase reg'n for threshold features that are all 1 or 0 on presences
   tmindev <- sapply(1:ncol(mm), function(i) {
      ifelse(isthreshold(colnames(mm)[i]) && (sum(mm[,i])==0 || sum(mm[,i])==nrow(mm)), 1,0)})
   pmax(0.001 * (apply(m,2,max)-apply(m,2,min)), hmindev, tmindev, apply(as.matrix(mm), 2, sd) * classregularization)
}
