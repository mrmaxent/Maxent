thresholds <-
function(x, nknots=50)
{
   min <- min(x)
   max <- max(x)
   k <- seq(min, max, length=nknots+2)[2:nknots+1]
   f <- outer(x, k, function(w,t) ifelse(w>=t,1,0))
   colnames(f) <- paste("",k, sep=":")
   f
}
