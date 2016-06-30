maxnet <-
function(p, data, f=maxnet.formula(p, data), regmult=1.0, regfun=maxnet.default.regularization, ...)
{
   mm <- model.matrix(f, data)
   reg <- regfun(p,mm) * regmult
   weights <- p+(1-p)*100
   glmnet.control(pmin=1.0e-8, fdev=0)  
   model <- glmnet(x=mm, y=as.factor(p), family="binomial", standardize=F, penalty.factor=reg, lambda=10^(seq(4,0,length.out=100))*sum(reg)/length(reg)*sum(p)/sum(weights), weights=weights, ...)
   class(model) <- c("maxnet", class(model))
   bb <- model$beta[,100]
   model$betas <- bb[bb!=0]
   model$alpha <- 0
   rr <- predict.maxnet(model, data[p==0,], type="exponent", clamp=F)
   raw <- rr / sum(rr)
   model$entropy <- -sum(raw * log(raw))
   model$alpha <- -log(sum(rr))
   model$penalty.factor <- reg
   model$featuremins <- apply(mm, 2, min)
   model$featuremaxs <- apply(mm, 2, max)
   vv <- (sapply(data, class)!="factor")
   model$varmin <- apply(data[,vv], 2, min)
   model$varmax <- apply(data[,vv], 2, max)
   means <- apply(data[p==1,vv], 2, mean)
   majorities <- sapply(names(data)[!vv], 
      function(n) which.max(table(data[p==1,n])))
   names(majorities) <- names(data)[!vv]
   model$samplemeans <- c(means, majorities)
   model$levels <- lapply(data, levels)
   model
}
