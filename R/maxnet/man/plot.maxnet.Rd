\name{plot.maxnet}
\alias{plot.maxnet}
\alias{response.plot}
\title{
  Response plots for maxnet models
}
\description{
  Create response plots for each predictor in a maxnet model
}
\usage{
\method{plot}{maxnet}(x, vars = names(x$samplemeans), common.scale = T, type = c("link", "exponential", "cloglog", "logistic"), ylab = NULL, ...)

response.plot(mod, v, type, mm=mod$samplemeans, min=mod$varmin[v], max=mod$varmax[v], levels=unlist(mod$levels[v]), plot=T, ylim=NULL, ylab=NULL)
}
\arguments{
  \item{x}{ an object of class \code{maxnet}, i.e., a fitted model. }
  \item{vars}{ vector of predictors for which response plots are desired. }
  \item{common.scale}{ if true, all plots use the same scale on the y-axis. }
  \item{type}{ type of response to plot on y-axis. }
  \item{ylab}{ label for y-axis. }
  \item{mod}{ a fitted model, must be of type \code{maxnet} if default values used for other arguments. }
  \item{v}{ name of variable to be plotted. }
  \item{mm}{ sample means (or majorities for factors) for predictors; predictors other than \code{v} are given these values. }
  \item{min}{ minimum value of \code{v}; determines range of x-axis }
  \item{max}{ maximum value of \code{v}; determines range of x-axis }
  \item{levels}{ if \code{v} is a factor, determines levels to be plotted }
  \item{plot}{ if false, don't draw the plot }
  \item{ylim}{ passed to plot() }
  \item{...}{ not used }
}
\value{
  If \code{plot} is false, return a vector of y values, one for each factor or 100 ranging from \code{min - 0.1*(max-min)} to \code{max + 0.1*(max-min)}.
}
\author{
  Steven Phillips
}
\keyword{ Maxent }
