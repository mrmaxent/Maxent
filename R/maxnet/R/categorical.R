categorical <-
function(x)
{
   f <- outer(x, levels(x), function(w,f) ifelse(w==f,1,0))
   colnames(f) <- paste("", levels(x), sep=":")
   f
}
