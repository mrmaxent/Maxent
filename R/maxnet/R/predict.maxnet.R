predict.maxnet <-
function(object, newdata, clamp=T, type=c("link","exponential","cloglog","logistic"), ...)
{
   if (clamp) {
      for (v in intersect(names(object$varmax), names(newdata))) {
         newdata[,v] <- pmin(pmax(newdata[,v], object$varmin[v]), object$varmax[v])
      }
   }
   terms <- sub("hinge\\((.*)\\):(.*):(.*)$", "hingeval(\\1,\\2,\\3)", names(object$betas))
   terms <- sub("categorical\\((.*)\\):(.*)$", "categoricalval(\\1,\\2)", terms)
   terms <- sub("thresholds\\((.*)\\):(.*)$", "thresholdval(\\1,\\2)", terms)
   f <- formula(paste("~", paste(terms, collapse=" + "), "-1"))
   mm <- model.matrix(f, data.frame(newdata))
   if (clamp) mm <- t(pmin(pmax(t(mm), object$featuremins[names(object$betas)]), 
                 object$featuremaxs[names(object$betas)]))
   link <- (mm %*% object$betas) + object$alpha
   type <- match.arg(type)
   if (type=="link") return(link)
   if (type=="exponential") return(exp(link))
   if (type=="cloglog") return(1-exp(0-exp(object$entropy+link)))
   if (type=="logistic") return(1/(1+exp(-object$entropy-link)))
}
