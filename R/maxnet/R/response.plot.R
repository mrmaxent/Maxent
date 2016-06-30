response.plot <-
function(mod, v, type, mm=mod$samplemeans, min=mod$varmin[v], max=mod$varmax[v], levels=unlist(mod$levels[v]), plot=T, ylim=NULL, ylab=NULL) {
   nr <- if (is.null(levels)) 100 else length(levels)
   m <- data.frame(matrix(mm,nr,length(mm),byrow=T))
   colnames(m) <- names(mm)
   m[,v] <- if (!is.null(levels)) levels else 
      seq(min - 0.1*(max-min), max+0.1*(max-min), length=100)
   preds <- predict(mod, m, type=type)
   if (is.null(ylab))
      ylab <- paste(toupper(substring(type,1,1)), substring(type, 2), sep="")
   if (plot) {
      if (is.null(levels)) {
         plot(m[,v], preds, xlab=v, ylab=ylab, type="l", ylim=ylim) 
      } else {
         barplot(as.vector(preds), names.arg=levels, xlab=v, ylab=ylab, ylim=ylim)
      }
   }
   else return(preds)
}
