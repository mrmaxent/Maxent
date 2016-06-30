\name{maxnet}
\alias{maxnet}
\alias{predict.maxnet}
\alias{maxnet.formula}
\alias{maxnet.default.regularization}
\title{Maxent over glmnet}
\description{
Maxent species distribution modeling using glmnet for model fitting
}
\usage{
maxnet(p, data, f = maxnet.formula(p, data), regmult = 1, regfun = maxnet.default.regularization, ...)
maxnet.default.regularization(p, m)

\method{predict}{maxnet}(object, newdata, clamp=T, type=c("link","exponential","cloglog","logistic"), ...)
\method{maxnet}{formula}(p, data, classes="default")
}
\arguments{
  \item{p}{ a vector of 1 (for presence) or 0 (for background). }
  \item{data}{ a matrix or data frame of predictor variables. }
  \item{f}{ a formula to determine the features to be used. }
  \item{regmult}{ a constant to adjust regularization. }
  \item{regfun}{ a function to compute regularization constant for each feature. }
  \item{object}{ an object of class \code{"maxnet"}, i.e., a fitted model. }
  \item{newdata}{ values of predictor variables to predict to. }
  \item{m}{ a matrix of feature values. }
  \item{clamp}{ if true, predictors and features are restricted to the range seen during model training. }
  \item{type}{ type of response required. }
  \item{classes}{ continuous feature classes desired, either "default" or any subset of "lqpht" (for example, "lh"). }
  \item{\dots}{ not used. }
}
\details{
  Using \code{lp} for the linear predictor and \code{entropy} for the entropy of the exponential model over the background data, the values plotted on the y-axis are:

  \code{lp} if \code{type} is "link".

  \code{exp(lp)} if \code{type} is "exponential".

  \code{1-exp(-exp(entropy+lp))} if \code{type} is "cloglog".

  \code{1/(1+exp(-entropy-lp))} if \code{type} is "logistic".
}
\value{
Maxnet returns an object of class \code{maxnet}, which is a list consisting of a glmnet model with the following elements added:
  \item{betas}{ nonzero coefficients of the fitted model }
  \item{alpha}{ constant offset making the exponential model sum to one over the background data }
  \item{entropy}{ entropy of the exponential model }
  \item{penalty.factor}{ the regularization constants used for each feature }
  \item{featuremins}{ minimum of each feature, to be used for clamping }
  \item{featuremaxs}{ maximum of each feature, to be used for clamping }
  \item{varmin}{ minimum of each predictor, to be used for clamping }
  \item{varmax}{ maximum of each predictor, to be used for clamping }
  \item{samplemeans}{ mean of each predictor over samples (majority for factors) }
  \item{levels}{ levels of each predictor that is a factor }
}
\author{
  Steven Phillips
}
\examples{
data(bradypus)
p <- bradypus$presence
data <- bradypus[,-1]
mod <- maxnet(p, data)
plot(mod, type="cloglog")
mod <- maxnet(p, data, maxnet.formula(p, data, classes="lq"))
plot(mod, "tmp6190_ann")
}
\keyword{Maxent}
\keyword{glmnet}
