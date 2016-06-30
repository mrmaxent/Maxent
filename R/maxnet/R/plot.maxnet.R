plot.maxnet <-
function(x, vars=names(x$samplemeans), common.scale=T, type=c("link","exponential","cloglog","logistic"), ylab=NULL, ...)
{
   type <- match.arg(type)
   nc <- ceiling(sqrt(length(vars)))
   nr <- ceiling(length(vars)/nc)
   par(mfrow=c(nr,nc), mar=c(5,5,4,2)+.1)
   ylim=NULL
   if (common.scale && (type=="link" || type=="exponential")) {
vals <- do.call(c, lapply(vars, function(v) 
          response.plot(x, v, type, plot=F)))
        ylim=c(min(vals), max(vals))
   }
   if (type=="cloglog" || type=="logistic") ylim=c(0,1)
   for (v in vars) response.plot(x, v, type, ylim=ylim, ylab=ylab)
}
