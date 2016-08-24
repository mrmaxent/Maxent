package density;

// Automatically created

/**
 * Params is the main class for querying and adjusting parameters for MaxEnt.
 * <p>
 * Each parameter can be queried or set with its own typesafe methods.
 * More general non-typesafe methods for setting one or more parameters
 * are available in the parent class.
 * <p>
 * Typical usage to set up some parameters and make a Maxent model is:
 * <ul>
 * <li><code> Params params = new Params();</code>
 * <li><i>  ... set some parameters using the methods in this package </i>
 * <li><code> params.setSelections();</code>
 * <li><code> Runner runner = new Runner(params);</code>
 * <li><code> runner.start();</code>
 * <li><code> runner.end();</code>
 * </ul>
 * <p>
 * The <code>params.setSelections()</code> method is needed only if one or more of the toggle parameters have been used (toggle species selected, toggle layer type, toggle layer selected).
 */
public class Params extends ParamsPre {
  /**
   * Get the type of a parameter, or <code>null</code> if the parameter doesn't exist
   * @param param the parameter
   * @return The parameter type
   */
  public String getType(String param) { if (!isParam(param)) return null; return getParameter(param).typename(); }
  /**
   * Get a list of all Maxent parameters
   * @return The list of parameters
   */
   public String[] getParameters() { return new String[] { "responsecurves" ,"pictures" ,"jackknife" ,"outputformat" ,"outputfiletype" ,"outputdirectory" ,"projectionlayers" ,"samplesfile" ,"environmentallayers" ,"randomseed" ,"logscale" ,"warnings" ,"tooltips" ,"askoverwrite" ,"skipifexists" ,"removeduplicates" ,"writeclampgrid" ,"writemess" ,"randomtestpoints" ,"betamultiplier" ,"maximumbackground" ,"biasfile" ,"testsamplesfile" ,"replicates" ,"replicatetype" ,"perspeciesresults" ,"writebackgroundpredictions" ,"biasisbayesianprior" ,"responsecurvesexponent" ,"linear" ,"quadratic" ,"product" ,"threshold" ,"hinge" ,"polyhedral" ,"addsamplestobackground" ,"addallsamplestobackground" ,"autorun" ,"dosqrtcat" ,"writeplotdata" ,"fadebyclamping" ,"extrapolate" ,"visible" ,"autofeature" ,"givemaxaucestimate" ,"doclamp" ,"outputgrids" ,"plots" ,"appendtoresultsfile" ,"parallelupdatefrequency" ,"maximumiterations" ,"convergencethreshold" ,"adjustsampleradius" ,"threads" ,"lq2lqptthreshold" ,"l2lqthreshold" ,"hingethreshold" ,"beta_threshold" ,"beta_categorical" ,"beta_lqp" ,"beta_hinge" ,"biastype" ,"logfile" ,"scientificpattern" ,"cache" ,"cachefeatures" ,"defaultprevalence" ,"applythresholdrule" ,"togglelayertype" ,"togglespeciesselected" ,"togglelayerselected" ,"verbose" ,"allowpartialdata" ,"prefixes" ,"printversion" ,"nodata" ,"nceas" ,"factorbiasout" ,"priordistribution" ,"debiasaverages" ,"minclamping" ,"manualreplicates" }; }
   /**
   * Set value of <i>responsecurves</i> parameter: Create graphs showing how predicted relative probability of occurrence depends on the value of each environmental variable
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setResponsecurves(boolean value) { setValue("responsecurves", value); }
   /**
   * Get value of <i>responsecurves</i> parameter: Create graphs showing how predicted relative probability of occurrence depends on the value of each environmental variable
   * @return The value <i>responsecurves</i> parameter
   */
   public boolean isResponsecurves() { return getboolean("responsecurves"); }
   /**
   * Set value of <i>pictures</i> parameter: Create a .png image for each output grid
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setPictures(boolean value) { setValue("pictures", value); }
   /**
   * Get value of <i>pictures</i> parameter: Create a .png image for each output grid
   * @return The value <i>pictures</i> parameter
   */
   public boolean isPictures() { return getboolean("pictures"); }
   /**
   * Set value of <i>jackknife</i> parameter: Measure importance of each environmental variable by training with each environmental variable first omitted, then used in isolation
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setJackknife(boolean value) { setValue("jackknife", value); }
   /**
   * Get value of <i>jackknife</i> parameter: Measure importance of each environmental variable by training with each environmental variable first omitted, then used in isolation
   * @return The value <i>jackknife</i> parameter
   */
   public boolean isJackknife() { return getboolean("jackknife"); }
   /**
   * Set value of <i>outputformat</i> parameter: Representation of probabilities used in writing output grids.  See Help for details
   * <p>
   * Default value is cloglog.
   * @param value the new value
   */
   public void setOutputformat(String value) { setValue("outputformat", value); }
   /**
   * Get value of <i>outputformat</i> parameter: Representation of probabilities used in writing output grids.  See Help for details
   * @return The value <i>outputformat</i> parameter
   */
   public String getOutputformat() { return getString("outputformat"); }
   /**
   * Set value of <i>outputfiletype</i> parameter: File format used for writing output grids
   * <p>
   * Default value is asc.
   * @param value the new value
   */
   public void setOutputfiletype(String value) { setValue("outputfiletype", value); }
   /**
   * Get value of <i>outputfiletype</i> parameter: File format used for writing output grids
   * @return The value <i>outputfiletype</i> parameter
   */
   public String getOutputfiletype() { return getString("outputfiletype"); }
   /**
   * Set value of <i>outputdirectory</i> parameter: Directory where outputs will be written.  This should be different from the environmental layers directory.
   * @param value the new value
   */
   public void setOutputdirectory(String value) { setValue("outputdirectory", value); }
   /**
   * Get value of <i>outputdirectory</i> parameter: Directory where outputs will be written.  This should be different from the environmental layers directory.
   * @return The value <i>outputdirectory</i> parameter
   */
   public String getOutputdirectory() { return getString("outputdirectory"); }
   /**
   * Set value of <i>projectionlayers</i> parameter: Location of an alternate set of environmental variables.  Maxent models will be projected onto these variables.<br>Can be a .csv file (in SWD format) or a directory containing one file per variable.<br>Multiple projection files/directories can be separated by commas.
   * @param value the new value
   */
   public void setProjectionlayers(String value) { setValue("projectionlayers", value); }
   /**
   * Get value of <i>projectionlayers</i> parameter: Location of an alternate set of environmental variables.  Maxent models will be projected onto these variables.<br>Can be a .csv file (in SWD format) or a directory containing one file per variable.<br>Multiple projection files/directories can be separated by commas.
   * @return The value <i>projectionlayers</i> parameter
   */
   public String getProjectionlayers() { return getString("projectionlayers"); }
   /**
   * Set value of <i>samplesfile</i> parameter: Please enter the name of a file containing presence locations for one or more species.
   * @param value the new value
   */
   public void setSamplesfile(String value) { setValue("samplesfile", value); }
   /**
   * Get value of <i>samplesfile</i> parameter: Please enter the name of a file containing presence locations for one or more species.
   * @return The value <i>samplesfile</i> parameter
   */
   public String getSamplesfile() { return getString("samplesfile"); }
   /**
   * Set value of <i>environmentallayers</i> parameter: Environmental variables can be in a directory containing one file per variable, <br>or all together in a .csv file in SWD format.  Please enter a directory name or file name.
   * @param value the new value
   */
   public void setEnvironmentallayers(String value) { setValue("environmentallayers", value); }
   /**
   * Get value of <i>environmentallayers</i> parameter: Environmental variables can be in a directory containing one file per variable, <br>or all together in a .csv file in SWD format.  Please enter a directory name or file name.
   * @return The value <i>environmentallayers</i> parameter
   */
   public String getEnvironmentallayers() { return getString("environmentallayers"); }
   /**
   * Set value of <i>randomseed</i> parameter: If selected, a different random seed will be used for each run, so a different random test/train partition<br>will be made and a different random subset of the background will be used, if applicable.
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setRandomseed(boolean value) { setValue("randomseed", value); }
   /**
   * Get value of <i>randomseed</i> parameter: If selected, a different random seed will be used for each run, so a different random test/train partition<br>will be made and a different random subset of the background will be used, if applicable.
   * @return The value <i>randomseed</i> parameter
   */
   public boolean isRandomseed() { return getboolean("randomseed"); }
   /**
   * Set value of <i>logscale</i> parameter: If selected, all pictures of models will use a logarithmic scale for color-coding.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setLogscale(boolean value) { setValue("logscale", value); }
   /**
   * Get value of <i>logscale</i> parameter: If selected, all pictures of models will use a logarithmic scale for color-coding.
   * @return The value <i>logscale</i> parameter
   */
   public boolean isLogscale() { return getboolean("logscale"); }
   /**
   * Set value of <i>warnings</i> parameter: Pop up windows to warn about potential problems with input data.<br>Regardless of this setting, warnings are always printed to the log file.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setWarnings(boolean value) { setValue("warnings", value); }
   /**
   * Get value of <i>warnings</i> parameter: Pop up windows to warn about potential problems with input data.<br>Regardless of this setting, warnings are always printed to the log file.
   * @return The value <i>warnings</i> parameter
   */
   public boolean isWarnings() { return getboolean("warnings"); }
   /**
   * Set value of <i>tooltips</i> parameter: Show messages that explain various parts of the interface, like this message
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setTooltips(boolean value) { setValue("tooltips", value); }
   /**
   * Get value of <i>tooltips</i> parameter: Show messages that explain various parts of the interface, like this message
   * @return The value <i>tooltips</i> parameter
   */
   public boolean isTooltips() { return getboolean("tooltips"); }
   /**
   * Set value of <i>askoverwrite</i> parameter: If output files already exist for a species being modeled,<br>pop up a window asking whether to overwrite or skip.  Default is to overwrite.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setAskoverwrite(boolean value) { setValue("askoverwrite", value); }
   /**
   * Get value of <i>askoverwrite</i> parameter: If output files already exist for a species being modeled,<br>pop up a window asking whether to overwrite or skip.  Default is to overwrite.
   * @return The value <i>askoverwrite</i> parameter
   */
   public boolean isAskoverwrite() { return getboolean("askoverwrite"); }
   /**
   * Set value of <i>skipifexists</i> parameter: If output files already exist for a species being modeled,<br>skip the species without remaking the model.
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setSkipifexists(boolean value) { setValue("skipifexists", value); }
   /**
   * Get value of <i>skipifexists</i> parameter: If output files already exist for a species being modeled,<br>skip the species without remaking the model.
   * @return The value <i>skipifexists</i> parameter
   */
   public boolean isSkipifexists() { return getboolean("skipifexists"); }
   /**
   * Set value of <i>removeduplicates</i> parameter: Remove duplicate presence records.<br>If environmental data are in grids, duplicates are records in the same grid cell.<br>Otherwise, duplicates are records with identical coordinates.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setRemoveduplicates(boolean value) { setValue("removeduplicates", value); }
   /**
   * Get value of <i>removeduplicates</i> parameter: Remove duplicate presence records.<br>If environmental data are in grids, duplicates are records in the same grid cell.<br>Otherwise, duplicates are records with identical coordinates.
   * @return The value <i>removeduplicates</i> parameter
   */
   public boolean isRemoveduplicates() { return getboolean("removeduplicates"); }
   /**
   * Set value of <i>writeclampgrid</i> parameter: Write a grid that shows the spatial distribution of clamping.<br>At each point, the value is the absolute difference between prediction values with and without clamping.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setWriteclampgrid(boolean value) { setValue("writeclampgrid", value); }
   /**
   * Get value of <i>writeclampgrid</i> parameter: Write a grid that shows the spatial distribution of clamping.<br>At each point, the value is the absolute difference between prediction values with and without clamping.
   * @return The value <i>writeclampgrid</i> parameter
   */
   public boolean isWriteclampgrid() { return getboolean("writeclampgrid"); }
   /**
   * Set value of <i>writemess</i> parameter: A multidimensional environmental similarity surface (MESS) shows where novel climate conditions exist in the projection layers.<br>The analysis shows both the degree of novelness and the variable that is most out of range at each point.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setWritemess(boolean value) { setValue("writemess", value); }
   /**
   * Get value of <i>writemess</i> parameter: A multidimensional environmental similarity surface (MESS) shows where novel climate conditions exist in the projection layers.<br>The analysis shows both the degree of novelness and the variable that is most out of range at each point.
   * @return The value <i>writemess</i> parameter
   */
   public boolean isWritemess() { return getboolean("writemess"); }
   /**
   * Set value of <i>randomtestpoints</i> parameter: Percentage of presence localities to be randomly set aside as test points, used to compute AUC, omission etc.
   * <p>
   * Default value is 0.
   * @param value the new value
   */
   public void setRandomtestpoints(int value) { setValue("randomtestpoints", value); }
   /**
   * Get value of <i>randomtestpoints</i> parameter: Percentage of presence localities to be randomly set aside as test points, used to compute AUC, omission etc.
   * @return The value <i>randomtestpoints</i> parameter
   */
   public int getRandomtestpoints() { return getint("randomtestpoints"); }
   /**
   * Set value of <i>betamultiplier</i> parameter: Multiply all automatic regularization parameters by this number.  A higher number gives a more spread-out distribution.
   * <p>
   * Default value is 1.0.
   * @param value the new value
   */
   public void setBetamultiplier(double value) { setValue("betamultiplier", value); }
   /**
   * Get value of <i>betamultiplier</i> parameter: Multiply all automatic regularization parameters by this number.  A higher number gives a more spread-out distribution.
   * @return The value <i>betamultiplier</i> parameter
   */
   public double getBetamultiplier() { return getdouble("betamultiplier"); }
   /**
   * Set value of <i>maximumbackground</i> parameter: If the number of background points / grid cells is larger than this number, then this number of cells is chosen randomly for background points
   * <p>
   * Default value is 10000.
   * @param value the new value
   */
   public void setMaximumbackground(int value) { setValue("maximumbackground", value); }
   /**
   * Get value of <i>maximumbackground</i> parameter: If the number of background points / grid cells is larger than this number, then this number of cells is chosen randomly for background points
   * @return The value <i>maximumbackground</i> parameter
   */
   public int getMaximumbackground() { return getint("maximumbackground"); }
   /**
   * Set value of <i>biasfile</i> parameter: Sampling is assumed to be biased according to the sampling distribution given in this grid file.<br>Values in this file must not be zero or negative.  MaxEnt will factor out the bias.<br>Requires environmental data to be in grids, rather than a SWD format file
   * @param value the new value
   */
   public void setBiasfile(String value) { setValue("biasfile", value); }
   /**
   * Get value of <i>biasfile</i> parameter: Sampling is assumed to be biased according to the sampling distribution given in this grid file.<br>Values in this file must not be zero or negative.  MaxEnt will factor out the bias.<br>Requires environmental data to be in grids, rather than a SWD format file
   * @return The value <i>biasfile</i> parameter
   */
   public String getBiasfile() { return getString("biasfile"); }
   /**
   * Set value of <i>testsamplesfile</i> parameter: Use the presence localities in this file to compute statistics (AUC, omission etc.)<br>The file can contain different localities for different species.<br>It takes precedence over the random test percentage.
   * @param value the new value
   */
   public void setTestsamplesfile(String value) { setValue("testsamplesfile", value); }
   /**
   * Get value of <i>testsamplesfile</i> parameter: Use the presence localities in this file to compute statistics (AUC, omission etc.)<br>The file can contain different localities for different species.<br>It takes precedence over the random test percentage.
   * @return The value <i>testsamplesfile</i> parameter
   */
   public String getTestsamplesfile() { return getString("testsamplesfile"); }
   /**
   * Set value of <i>replicates</i> parameter: Number of replicate runs to do when cross-validating, bootstrapping or doing sampling with replacement runs
   * <p>
   * Default value is 1.
   * @param value the new value
   */
   public void setReplicates(int value) { setValue("replicates", value); }
   /**
   * Get value of <i>replicates</i> parameter: Number of replicate runs to do when cross-validating, bootstrapping or doing sampling with replacement runs
   * @return The value <i>replicates</i> parameter
   */
   public int getReplicates() { return getint("replicates"); }
   /**
   * Set value of <i>replicatetype</i> parameter: If replicates is greater than 1, do multiple runs of this type:<br>Crossvalidate: samples divided into <i>replicates</i> folds; each fold in turn used for test data.<br>Bootstrap: replicate sample sets chosen by sampling with replacement.<br>Subsample: replicate sample sets chosen by removing <i>random test percentage</i> without replacement to be used for evaluation.
   * <p>
   * Default value is crossvalidate.
   * @param value the new value
   */
   public void setReplicatetype(String value) { setValue("replicatetype", value); }
   /**
   * Get value of <i>replicatetype</i> parameter: If replicates is greater than 1, do multiple runs of this type:<br>Crossvalidate: samples divided into <i>replicates</i> folds; each fold in turn used for test data.<br>Bootstrap: replicate sample sets chosen by sampling with replacement.<br>Subsample: replicate sample sets chosen by removing <i>random test percentage</i> without replacement to be used for evaluation.
   * @return The value <i>replicatetype</i> parameter
   */
   public String getReplicatetype() { return getString("replicatetype"); }
   /**
   * Set value of <i>perspeciesresults</i> parameter: Write separate maxentResults file for each species
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setPerspeciesresults(boolean value) { setValue("perspeciesresults", value); }
   /**
   * Get value of <i>perspeciesresults</i> parameter: Write separate maxentResults file for each species
   * @return The value <i>perspeciesresults</i> parameter
   */
   public boolean isPerspeciesresults() { return getboolean("perspeciesresults"); }
   /**
   * Set value of <i>writebackgroundpredictions</i> parameter: Write .csv file with predictions at background points
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setWritebackgroundpredictions(boolean value) { setValue("writebackgroundpredictions", value); }
   /**
   * Get value of <i>writebackgroundpredictions</i> parameter: Write .csv file with predictions at background points
   * @return The value <i>writebackgroundpredictions</i> parameter
   */
   public boolean isWritebackgroundpredictions() { return getboolean("writebackgroundpredictions"); }
   /**
   * Set value of <i>biasisbayesianprior</i> parameter: Bias file is really a Bayesian prior
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setBiasisbayesianprior(boolean value) { setValue("biasisbayesianprior", value); }
   /**
   * Get value of <i>biasisbayesianprior</i> parameter: Bias file is really a Bayesian prior
   * @return The value <i>biasisbayesianprior</i> parameter
   */
   public boolean isBiasisbayesianprior() { return getboolean("biasisbayesianprior"); }
   /**
   * Set value of <i>responsecurvesexponent</i> parameter: Instead of showing the logistic value for the y axis in response curves, show the exponent (a linear combination of features)
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setResponsecurvesexponent(boolean value) { setValue("responsecurvesexponent", value); }
   /**
   * Get value of <i>responsecurvesexponent</i> parameter: Instead of showing the logistic value for the y axis in response curves, show the exponent (a linear combination of features)
   * @return The value <i>responsecurvesexponent</i> parameter
   */
   public boolean isResponsecurvesexponent() { return getboolean("responsecurvesexponent"); }
   /**
   * Set value of <i>linear</i> parameter: Allow linear features to be used
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setLinear(boolean value) { setValue("linear", value); }
   /**
   * Get value of <i>linear</i> parameter: Allow linear features to be used
   * @return The value <i>linear</i> parameter
   */
   public boolean isLinear() { return getboolean("linear"); }
   /**
   * Set value of <i>quadratic</i> parameter: Allow quadratic features to be used
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setQuadratic(boolean value) { setValue("quadratic", value); }
   /**
   * Get value of <i>quadratic</i> parameter: Allow quadratic features to be used
   * @return The value <i>quadratic</i> parameter
   */
   public boolean isQuadratic() { return getboolean("quadratic"); }
   /**
   * Set value of <i>product</i> parameter: Allow product features to be used
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setProduct(boolean value) { setValue("product", value); }
   /**
   * Get value of <i>product</i> parameter: Allow product features to be used
   * @return The value <i>product</i> parameter
   */
   public boolean isProduct() { return getboolean("product"); }
   /**
   * Set value of <i>threshold</i> parameter: Allow threshold features to be used
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setThreshold(boolean value) { setValue("threshold", value); }
   /**
   * Get value of <i>threshold</i> parameter: Allow threshold features to be used
   * @return The value <i>threshold</i> parameter
   */
   public boolean isThreshold() { return getboolean("threshold"); }
   /**
   * Set value of <i>hinge</i> parameter: Allow hinge features to be used
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setHinge(boolean value) { setValue("hinge", value); }
   /**
   * Get value of <i>hinge</i> parameter: Allow hinge features to be used
   * @return The value <i>hinge</i> parameter
   */
   public boolean isHinge() { return getboolean("hinge"); }
   /**
   * Set value of <i>polyhedral</i> parameter: 
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setPolyhedral(boolean value) { setValue("polyhedral", value); }
   /**
   * Get value of <i>polyhedral</i> parameter: 
   * @return The value <i>polyhedral</i> parameter
   */
   public boolean isPolyhedral() { return getboolean("polyhedral"); }
   /**
   * Set value of <i>addsamplestobackground</i> parameter: Add to the background any sample for which has a combination of environmental values that isn't already present in the background
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setAddsamplestobackground(boolean value) { setValue("addsamplestobackground", value); }
   /**
   * Get value of <i>addsamplestobackground</i> parameter: Add to the background any sample for which has a combination of environmental values that isn't already present in the background
   * @return The value <i>addsamplestobackground</i> parameter
   */
   public boolean isAddsamplestobackground() { return getboolean("addsamplestobackground"); }
   /**
   * Set value of <i>addallsamplestobackground</i> parameter: Add all samples to the background, even if they have combinations of environmental values that are already present in the background
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setAddallsamplestobackground(boolean value) { setValue("addallsamplestobackground", value); }
   /**
   * Get value of <i>addallsamplestobackground</i> parameter: Add all samples to the background, even if they have combinations of environmental values that are already present in the background
   * @return The value <i>addallsamplestobackground</i> parameter
   */
   public boolean isAddallsamplestobackground() { return getboolean("addallsamplestobackground"); }
   /**
   * Set value of <i>autorun</i> parameter: Start running as soon as the the program starts up
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setAutorun(boolean value) { setValue("autorun", value); }
   /**
   * Get value of <i>autorun</i> parameter: Start running as soon as the the program starts up
   * @return The value <i>autorun</i> parameter
   */
   public boolean isAutorun() { return getboolean("autorun"); }
   /**
   * Set value of <i>dosqrtcat</i> parameter: 
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setDosqrtcat(boolean value) { setValue("dosqrtcat", value); }
   /**
   * Get value of <i>dosqrtcat</i> parameter: 
   * @return The value <i>dosqrtcat</i> parameter
   */
   public boolean isDosqrtcat() { return getboolean("dosqrtcat"); }
   /**
   * Set value of <i>writeplotdata</i> parameter: Write output files containing the data used to make response curves, for import into external plotting software
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setWriteplotdata(boolean value) { setValue("writeplotdata", value); }
   /**
   * Get value of <i>writeplotdata</i> parameter: Write output files containing the data used to make response curves, for import into external plotting software
   * @return The value <i>writeplotdata</i> parameter
   */
   public boolean isWriteplotdata() { return getboolean("writeplotdata"); }
   /**
   * Set value of <i>fadebyclamping</i> parameter: Reduce prediction at each point in projections by the difference between<br>clamped and non-clamped output at that point
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setFadebyclamping(boolean value) { setValue("fadebyclamping", value); }
   /**
   * Get value of <i>fadebyclamping</i> parameter: Reduce prediction at each point in projections by the difference between<br>clamped and non-clamped output at that point
   * @return The value <i>fadebyclamping</i> parameter
   */
   public boolean isFadebyclamping() { return getboolean("fadebyclamping"); }
   /**
   * Set value of <i>extrapolate</i> parameter: Predict to regions of environmental space outside the limits encountered during training
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setExtrapolate(boolean value) { setValue("extrapolate", value); }
   /**
   * Get value of <i>extrapolate</i> parameter: Predict to regions of environmental space outside the limits encountered during training
   * @return The value <i>extrapolate</i> parameter
   */
   public boolean isExtrapolate() { return getboolean("extrapolate"); }
   /**
   * Set value of <i>visible</i> parameter: Make the Maxent user interface visible
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setVisible(boolean value) { setValue("visible", value); }
   /**
   * Get value of <i>visible</i> parameter: Make the Maxent user interface visible
   * @return The value <i>visible</i> parameter
   */
   public boolean isVisible() { return getboolean("visible"); }
   /**
   * Set value of <i>autofeature</i> parameter: Automatically select which feature classes to use, based on number of training samples
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setAutofeature(boolean value) { setValue("autofeature", value); }
   /**
   * Get value of <i>autofeature</i> parameter: Automatically select which feature classes to use, based on number of training samples
   * @return The value <i>autofeature</i> parameter
   */
   public boolean isAutofeature() { return getboolean("autofeature"); }
   /**
   * Set value of <i>givemaxaucestimate</i> parameter: Write an estimate of the maximum achievable AUC in the html output, based on the extent of the Maxent distribution
   * <p>
   * Default value is true.
   * @param value the new value
   */
   void setGivemaxaucestimate(boolean value) { setValue("givemaxaucestimate", value); }
   /**
   * Get value of <i>givemaxaucestimate</i> parameter: Write an estimate of the maximum achievable AUC in the html output, based on the extent of the Maxent distribution
   * @return The value <i>givemaxaucestimate</i> parameter
   */
   public boolean isGivemaxaucestimate() { return getboolean("givemaxaucestimate"); }
   /**
   * Set value of <i>doclamp</i> parameter: Apply clamping when projecting
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setDoclamp(boolean value) { setValue("doclamp", value); }
   /**
   * Get value of <i>doclamp</i> parameter: Apply clamping when projecting
   * @return The value <i>doclamp</i> parameter
   */
   public boolean isDoclamp() { return getboolean("doclamp"); }
   /**
   * Set value of <i>outputgrids</i> parameter: Write output grids.  Turning this off when doing replicate runs causes only the summary grids (average, std deviation etc.) to be written, not those for the individual runs.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setOutputgrids(boolean value) { setValue("outputgrids", value); }
   /**
   * Get value of <i>outputgrids</i> parameter: Write output grids.  Turning this off when doing replicate runs causes only the summary grids (average, std deviation etc.) to be written, not those for the individual runs.
   * @return The value <i>outputgrids</i> parameter
   */
   public boolean isOutputgrids() { return getboolean("outputgrids"); }
   /**
   * Set value of <i>plots</i> parameter: Write various plots for inclusion in .html output
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setPlots(boolean value) { setValue("plots", value); }
   /**
   * Get value of <i>plots</i> parameter: Write various plots for inclusion in .html output
   * @return The value <i>plots</i> parameter
   */
   public boolean isPlots() { return getboolean("plots"); }
   /**
   * Set value of <i>appendtoresultsfile</i> parameter: If false, maxentResults.csv file is reinitialized before each run
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setAppendtoresultsfile(boolean value) { setValue("appendtoresultsfile", value); }
   /**
   * Get value of <i>appendtoresultsfile</i> parameter: If false, maxentResults.csv file is reinitialized before each run
   * @return The value <i>appendtoresultsfile</i> parameter
   */
   public boolean isAppendtoresultsfile() { return getboolean("appendtoresultsfile"); }
   /**
   * Set value of <i>parallelupdatefrequency</i> parameter: 
   * <p>
   * Default value is 30.
   * @param value the new value
   */
   void setParallelupdatefrequency(int value) { setValue("parallelupdatefrequency", value); }
   /**
   * Get value of <i>parallelupdatefrequency</i> parameter: 
   * @return The value <i>parallelupdatefrequency</i> parameter
   */
   public int getParallelupdatefrequency() { return getint("parallelupdatefrequency"); }
   /**
   * Set value of <i>maximumiterations</i> parameter: Stop training after this many iterations of the optimization algorithm
   * <p>
   * Default value is 500.
   * @param value the new value
   */
   public void setMaximumiterations(int value) { setValue("maximumiterations", value); }
   /**
   * Get value of <i>maximumiterations</i> parameter: Stop training after this many iterations of the optimization algorithm
   * @return The value <i>maximumiterations</i> parameter
   */
   public int getMaximumiterations() { return getint("maximumiterations"); }
   /**
   * Set value of <i>convergencethreshold</i> parameter: Stop training when the drop in log loss per iteration drops below this number
   * <p>
   * Default value is 1.0E-5.
   * @param value the new value
   */
   public void setConvergencethreshold(double value) { setValue("convergencethreshold", value); }
   /**
   * Get value of <i>convergencethreshold</i> parameter: Stop training when the drop in log loss per iteration drops below this number
   * @return The value <i>convergencethreshold</i> parameter
   */
   public double getConvergencethreshold() { return getdouble("convergencethreshold"); }
   /**
   * Set value of <i>adjustsampleradius</i> parameter: Add this number of pixels to the radius of white/purple dots for samples on pictures of predictions.<br>Negative values reduce size of dots.
   * <p>
   * Default value is 0.
   * @param value the new value
   */
   public void setAdjustsampleradius(int value) { setValue("adjustsampleradius", value); }
   /**
   * Get value of <i>adjustsampleradius</i> parameter: Add this number of pixels to the radius of white/purple dots for samples on pictures of predictions.<br>Negative values reduce size of dots.
   * @return The value <i>adjustsampleradius</i> parameter
   */
   public int getAdjustsampleradius() { return getint("adjustsampleradius"); }
   /**
   * Set value of <i>threads</i> parameter: Number of processor threads to use.  Matching this number to the number of cores on your computer speeds up some operations, especially variable jackknifing.
   * <p>
   * Default value is 1.
   * @param value the new value
   */
   public void setThreads(int value) { setValue("threads", value); }
   /**
   * Get value of <i>threads</i> parameter: Number of processor threads to use.  Matching this number to the number of cores on your computer speeds up some operations, especially variable jackknifing.
   * @return The value <i>threads</i> parameter
   */
   public int getThreads() { return getint("threads"); }
   /**
   * Set value of <i>lq2lqptthreshold</i> parameter: Number of samples at which product and threshold features start being used
   * <p>
   * Default value is 80.
   * @param value the new value
   */
   public void setLq2lqptthreshold(int value) { setValue("lq2lqptthreshold", value); }
   /**
   * Get value of <i>lq2lqptthreshold</i> parameter: Number of samples at which product and threshold features start being used
   * @return The value <i>lq2lqptthreshold</i> parameter
   */
   public int getLq2lqptthreshold() { return getint("lq2lqptthreshold"); }
   /**
   * Set value of <i>l2lqthreshold</i> parameter: Number of samples at which quadratic features start being used
   * <p>
   * Default value is 10.
   * @param value the new value
   */
   public void setL2lqthreshold(int value) { setValue("l2lqthreshold", value); }
   /**
   * Get value of <i>l2lqthreshold</i> parameter: Number of samples at which quadratic features start being used
   * @return The value <i>l2lqthreshold</i> parameter
   */
   public int getL2lqthreshold() { return getint("l2lqthreshold"); }
   /**
   * Set value of <i>hingethreshold</i> parameter: Number of samples at which hinge features start being used
   * <p>
   * Default value is 15.
   * @param value the new value
   */
   public void setHingethreshold(int value) { setValue("hingethreshold", value); }
   /**
   * Get value of <i>hingethreshold</i> parameter: Number of samples at which hinge features start being used
   * @return The value <i>hingethreshold</i> parameter
   */
   public int getHingethreshold() { return getint("hingethreshold"); }
   /**
   * Set value of <i>beta_threshold</i> parameter: Regularization parameter to be applied to all threshold features; negative value enables automatic setting
   * <p>
   * Default value is -1.0.
   * @param value the new value
   */
   public void setBeta_threshold(double value) { setValue("beta_threshold", value); }
   /**
   * Get value of <i>beta_threshold</i> parameter: Regularization parameter to be applied to all threshold features; negative value enables automatic setting
   * @return The value <i>beta_threshold</i> parameter
   */
   public double getBeta_threshold() { return getdouble("beta_threshold"); }
   /**
   * Set value of <i>beta_categorical</i> parameter: Regularization parameter to be applied to all categorical features; negative value enables automatic setting
   * <p>
   * Default value is -1.0.
   * @param value the new value
   */
   public void setBeta_categorical(double value) { setValue("beta_categorical", value); }
   /**
   * Get value of <i>beta_categorical</i> parameter: Regularization parameter to be applied to all categorical features; negative value enables automatic setting
   * @return The value <i>beta_categorical</i> parameter
   */
   public double getBeta_categorical() { return getdouble("beta_categorical"); }
   /**
   * Set value of <i>beta_lqp</i> parameter: Regularization parameter to be applied to all linear, quadratic and product features; negative value enables automatic setting
   * <p>
   * Default value is -1.0.
   * @param value the new value
   */
   public void setBeta_lqp(double value) { setValue("beta_lqp", value); }
   /**
   * Get value of <i>beta_lqp</i> parameter: Regularization parameter to be applied to all linear, quadratic and product features; negative value enables automatic setting
   * @return The value <i>beta_lqp</i> parameter
   */
   public double getBeta_lqp() { return getdouble("beta_lqp"); }
   /**
   * Set value of <i>beta_hinge</i> parameter: Regularization parameter to be applied to all hinge features; negative value enables automatic setting
   * <p>
   * Default value is -1.0.
   * @param value the new value
   */
   public void setBeta_hinge(double value) { setValue("beta_hinge", value); }
   /**
   * Get value of <i>beta_hinge</i> parameter: Regularization parameter to be applied to all hinge features; negative value enables automatic setting
   * @return The value <i>beta_hinge</i> parameter
   */
   public double getBeta_hinge() { return getdouble("beta_hinge"); }
   /**
   * Set value of <i>biastype</i> parameter: Type of bias correction procedure to be used, if bias file is given
   * <p>
   * Default value is 0.
   * @param value the new value
   */
   void setBiastype(int value) { setValue("biastype", value); }
   /**
   * Get value of <i>biastype</i> parameter: Type of bias correction procedure to be used, if bias file is given
   * @return The value <i>biastype</i> parameter
   */
   public int getBiastype() { return getint("biastype"); }
   /**
   * Set value of <i>logfile</i> parameter: File name to be used for writing debugging information about a run in output directory
   * <p>
   * Default value is maxent.log.
   * @param value the new value
   */
   public void setLogfile(String value) { setValue("logfile", value); }
   /**
   * Get value of <i>logfile</i> parameter: File name to be used for writing debugging information about a run in output directory
   * @return The value <i>logfile</i> parameter
   */
   public String getLogfile() { return getString("logfile"); }
   /**
   * Set value of <i>scientificpattern</i> parameter: Pattern used to write scientific notation in output grids
   * <p>
   * Default value is #.#####e0.
   * @param value the new value
   */
   void setScientificpattern(String value) { setValue("scientificpattern", value); }
   /**
   * Get value of <i>scientificpattern</i> parameter: Pattern used to write scientific notation in output grids
   * @return The value <i>scientificpattern</i> parameter
   */
   public String getScientificpattern() { return getString("scientificpattern"); }
   /**
   * Set value of <i>cache</i> parameter: Make a .mxe cached version of ascii files, for faster access
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setCache(boolean value) { setValue("cache", value); }
   /**
   * Get value of <i>cache</i> parameter: Make a .mxe cached version of ascii files, for faster access
   * @return The value <i>cache</i> parameter
   */
   public boolean isCache() { return getboolean("cache"); }
   /**
   * Set value of <i>cachefeatures</i> parameter: Cache derived features (such as product features) in memory to speed up training
   * <p>
   * Default value is true.
   * @param value the new value
   */
   void setCachefeatures(boolean value) { setValue("cachefeatures", value); }
   /**
   * Get value of <i>cachefeatures</i> parameter: Cache derived features (such as product features) in memory to speed up training
   * @return The value <i>cachefeatures</i> parameter
   */
   public boolean isCachefeatures() { return getboolean("cachefeatures"); }
   /**
   * Set value of <i>defaultprevalence</i> parameter: Default prevalence of the species: probability of presence at ordinary occurrence points.<br>See Elith et al., Diversity and Distributions, 2011 for details.
   * <p>
   * Default value is 0.5.
   * @param value the new value
   */
   public void setDefaultprevalence(double value) { setValue("defaultprevalence", value); }
   /**
   * Get value of <i>defaultprevalence</i> parameter: Default prevalence of the species: probability of presence at ordinary occurrence points.<br>See Elith et al., Diversity and Distributions, 2011 for details.
   * @return The value <i>defaultprevalence</i> parameter
   */
   public double getDefaultprevalence() { return getdouble("defaultprevalence"); }
   /**
   * Set value of <i>applythresholdrule</i> parameter: Apply a threshold rule, generating a binary output grid in addition to the regular prediction grid.  Use the full name of the threshold rule in Maxent's html output as the argument.  For example, 'applyThresholdRule=Fixed cumulative value 1'.
   * @param value the new value
   */
   public void setApplythresholdrule(String value) { setValue("applythresholdrule", value); }
   /**
   * Get value of <i>applythresholdrule</i> parameter: Apply a threshold rule, generating a binary output grid in addition to the regular prediction grid.  Use the full name of the threshold rule in Maxent's html output as the argument.  For example, 'applyThresholdRule=Fixed cumulative value 1'.
   * @return The value <i>applythresholdrule</i> parameter
   */
   public String getApplythresholdrule() { return getString("applythresholdrule"); }
   /**
   * Toggle continuous/categorical for environmental layers whose names begin with this prefix (default: all continuous)
   * @param value the prefix
   */
   public void togglelayertype(String value) { parseParam("togglelayertype="+value); }
   /**
   * Toggle selection of species whose names begin with this prefix (default: all selected)
   * @param value the prefix
   */
   public void togglespeciesselected(String value) { parseParam("togglespeciesselected="+value); }
   /**
   * Toggle selection of environmental layers whose names begin with this prefix (default: all selected)
   * @param value the prefix
   */
   public void togglelayerselected(String value) { parseParam("togglelayerselected="+value); }
   /**
   * Set value of <i>verbose</i> parameter: Gived detailed diagnostics for debugging
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setVerbose(boolean value) { setValue("verbose", value); }
   /**
   * Get value of <i>verbose</i> parameter: Gived detailed diagnostics for debugging
   * @return The value <i>verbose</i> parameter
   */
   public boolean isVerbose() { return getboolean("verbose"); }
   /**
   * Set value of <i>allowpartialdata</i> parameter: During model training, allow use of samples that have nodata values for one or more environmental variables.
   * <p>
   * Default value is false.
   * @param value the new value
   */
   public void setAllowpartialdata(boolean value) { setValue("allowpartialdata", value); }
   /**
   * Get value of <i>allowpartialdata</i> parameter: During model training, allow use of samples that have nodata values for one or more environmental variables.
   * @return The value <i>allowpartialdata</i> parameter
   */
   public boolean isAllowpartialdata() { return getboolean("allowpartialdata"); }
   /**
   * Set value of <i>prefixes</i> parameter: When toggling samples or layers selected or layer types, allow toggle string to be a prefix rather than an exact match.
   * <p>
   * Default value is true.
   * @param value the new value
   */
   public void setPrefixes(boolean value) { setValue("prefixes", value); }
   /**
   * Get value of <i>prefixes</i> parameter: When toggling samples or layers selected or layer types, allow toggle string to be a prefix rather than an exact match.
   * @return The value <i>prefixes</i> parameter
   */
   public boolean isPrefixes() { return getboolean("prefixes"); }
   /**
   * Set value of <i>printversion</i> parameter: Print Maxent software version number and exit
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setPrintversion(boolean value) { setValue("printversion", value); }
   /**
   * Get value of <i>printversion</i> parameter: Print Maxent software version number and exit
   * @return The value <i>printversion</i> parameter
   */
   public boolean isPrintversion() { return getboolean("printversion"); }
   /**
   * Set value of <i>nodata</i> parameter: Value to be interpreted as nodata values in SWD sample data
   * <p>
   * Default value is -9999.
   * @param value the new value
   */
   public void setNodata(int value) { setValue("nodata", value); }
   /**
   * Get value of <i>nodata</i> parameter: Value to be interpreted as nodata values in SWD sample data
   * @return The value <i>nodata</i> parameter
   */
   public int getNodata() { return getint("nodata"); }
   /**
   * Set value of <i>nceas</i> parameter: 
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setNceas(boolean value) { setValue("nceas", value); }
   /**
   * Get value of <i>nceas</i> parameter: 
   * @return The value <i>nceas</i> parameter
   */
   public boolean isNceas() { return getboolean("nceas"); }
   /**
   * Set value of <i>factorbiasout</i> parameter: 
   * @param value the new value
   */
   void setFactorbiasout(String value) { setValue("factorbiasout", value); }
   /**
   * Get value of <i>factorbiasout</i> parameter: 
   * @return The value <i>factorbiasout</i> parameter
   */
   public String getFactorbiasout() { return getString("factorbiasout"); }
   /**
   * Set value of <i>priordistribution</i> parameter: 
   * @param value the new value
   */
   void setPriordistribution(String value) { setValue("priordistribution", value); }
   /**
   * Get value of <i>priordistribution</i> parameter: 
   * @return The value <i>priordistribution</i> parameter
   */
   public String getPriordistribution() { return getString("priordistribution"); }
   /**
   * Set value of <i>debiasaverages</i> parameter: 
   * @param value the new value
   */
   void setDebiasaverages(String value) { setValue("debiasaverages", value); }
   /**
   * Get value of <i>debiasaverages</i> parameter: 
   * @return The value <i>debiasaverages</i> parameter
   */
   public String getDebiasaverages() { return getString("debiasaverages"); }
   /**
   * Set value of <i>minclamping</i> parameter: If true, do clamping only at sites where it results in lower prediction.
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setMinclamping(boolean value) { setValue("minclamping", value); }
   /**
   * Get value of <i>minclamping</i> parameter: If true, do clamping only at sites where it results in lower prediction.
   * @return The value <i>minclamping</i> parameter
   */
   public boolean isMinclamping() { return getboolean("minclamping"); }
   /**
   * Set value of <i>manualreplicates</i> parameter: If true, species data has already been split into replicated runs in input.
   * <p>
   * Default value is false.
   * @param value the new value
   */
   void setManualreplicates(boolean value) { setValue("manualreplicates", value); }
   /**
   * Get value of <i>manualreplicates</i> parameter: If true, species data has already been split into replicated runs in input.
   * @return The value <i>manualreplicates</i> parameter
   */
   public boolean isManualreplicates() { return getboolean("manualreplicates"); }
}
