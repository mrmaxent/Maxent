hingeval <-
function(x, min, max)
{
   pmin(1, pmax(0, (x-min)/(max-min)))
}
