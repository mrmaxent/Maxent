maxnet.formula <-
function(p, data, classes="default")
{
   cont <- names(data)[sapply(data,class)!="factor"]
   categorical <- names(data)[sapply(data,class)=="factor"]
   np <- sum(p)
   if (classes=="default") {
      if (np < 10) classes <- "l"
      else if (np < 15) classes <- "lq"
      else if (np < 80) classes <- "lqh"
      else classes <- "lqpht"
   }
   terms <- NULL
   if (length(cont)) {
      if (grepl("l", classes))
         terms <- c(terms, paste(cont,collapse=" + "))
      if (grepl("q", classes))
         terms <- c(terms, paste("I(",cont,"^2)",sep="",collapse=" + "))
      if (grepl("h", classes))
         terms <- c(terms, paste("hinge(",cont,")",sep="",collapse=" + "))
      if (grepl("t", classes))
         terms <- c(terms, paste("thresholds(",cont,")",sep="",collapse=" + "))
      if (grepl("p", classes)) {
         m <- outer(cont, cont, function(x,y) paste(x,y,sep=":"))
         terms <- c(terms, m[lower.tri(m)])
      }
   }
   if (length(categorical))
      terms <- c(terms, paste("categorical(",categorical,")",sep="",collapse=" + "))
   formula(paste("~", paste(terms, collapse = " + "), "-1"))
}
