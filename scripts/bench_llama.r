require(llama)
require(aslib)

output = list()

compute_model <- function(model_name, folds) {
	learner = makeLearner(model_name)
	type <- strsplit(model_name, '.', fixed = TRUE)[[1]][1]
	if (type == "regr") {
		return(regression(learner, folds))
	} else {
		classify(learner, folds)
	}
}


print_info <- function(data, folds, model, name) {
	
	s = paste("", mean(parscores(data, vbs, addCosts=TRUE)), sep="")
	s = paste(s, mean(parscores(folds, model, addCosts=TRUE)), sep=",")
	return(paste(s, mean(parscores(data, singleBest, addCosts=TRUE)), sep=","))

}

# scenarios = readLines("./names.txt") 

scenarios = list("ASP-POTASSCO")

models = list("classif.J48", "classif.cforest", "classif.rpart", "regr.rpart", "regr.cforest")

output <- append(output, "scenario,model,vbs,mcp,singleb\n")

for (name in scenarios) {
	sc = invisible(getCosealASScenario(name))
	sc$feature.values = na.omit(sc$feature.values)
	data = convertToLlama(sc)
	folds =	convertToLlamaCVFolds(sc)
	
	for (model in models) {
		values = list()
		values <- append(values, name)
		values <- append(values, model)
		rest <- tryCatch(
			{
				m = compute_model(model, folds)
				new_line <- print_info(data, folds, m, model)
				rest <- (paste(new_line, "\n", sep=""))
			}, 
			error=function(e){
				# cat("ERROR :",conditionMessage(e), " on model: ", model, " and scenario: ", name, "\n")
				# values <- append(values, "-1,-1,-1")
				return("-1,-1,-1\n")
			}
		)

		values <- append(values, rest)
		line <- paste(values, collapse=",")
		output <- append(output, line)

	}

}

for (l in output) {
	cat(l)
}







