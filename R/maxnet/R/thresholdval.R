thresholdval <-
function(x, knot) 
{
   ifelse(x >= knot, 1, 0)
}
