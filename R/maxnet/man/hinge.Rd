\name{hinge}
\alias{hinge}
\alias{hingeval}
\alias{thresholds}
\alias{thresholdval}
\alias{categorical}
\alias{categoricalval}
\title{Maxent feature classes}
\description{Create and evaluate Maxent's feature classes}
\usage{
hinge(x, nknots = 50)
thresholds(x, nknots=50)
categorical(x)
hingeval(x, min, max)
thresholdval(x, knot)
categoricalval(x, category)
}
\arguments{
  \item{x}{ a predictor: a factor for categorical and categoricalval, otherwise numeric. }
  \item{nknots}{ number of knots. }
  \item{min}{ value of \code{x} at which hinge feature is 0. }
  \item{max}{ value of \code{x} at which hinge feature is 1. }
  \item{knot}{ value of \code{x} at which threshold feature changes from 0 to 1. }
  \item{category}{ a level of \code{x} }
}
\value{
  \code{hinge}, \code{threshold} and \code{categorical} return a matrix with a column for each feature of the specified type.  \code{hinge} creates \code{2*nknots} hinge features, half with \code{min=min(x)} and half with \code{max=max(x)}, and knots evenly spaced between \code{min(x)} and \code{max(x)}.  \code{hingeval} calculates a single hinge feature: 0 if the predictor is below min, 1 if the predictor is above max, and linearly interpolated inbetween.  \code{thresholdval} calculates a single threshold feature: 1 if the predictor is above the knot, 0 otherwise.  \code{categoricalval} calculates a categorical feature:  1 if the predictor matches the category.
}
\author{
  Steven Phillips
}
\keyword{Maxent}
