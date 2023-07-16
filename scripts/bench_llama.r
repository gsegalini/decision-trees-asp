require(llama)
require(aslib)

output = list()

compute_models <- function(name) {

	sc = invisible(getCosealASScenario(name))
	data = convertToLlama(sc)
	folds =	convertToLlamaCVFolds(sc)
	
	model_j48 = classify(makeLearner("classif.J48"), folds)
	model_random_forests = classify(makeLearner("classif.cforest"), folds)
	newList = list("data" = data, "folds" = folds, "j48" = model_j48, "rf" = model_random_forests)
	
	return(newList)
	
}


print_info <- function(data, folds, model, name) {
	
	s = paste(name, sum(misclassificationPenalties(data, vbs)), sep=",")
	s = paste(s, sum(misclassificationPenalties(folds, model)), sep=",")
	return(paste(s, sum(misclassificationPenalties(data, singleBest)), sep=","))

}

# scenarios = readLines("../data-preparation/aslib_data-master/names.txt") 

scenarios = list("ASP-POTASSCO", "CSP-Minizinc-Time-2016")

models = list("j48", "rf")
output <- append(output, "scenario,model,vbs,mcp,singleb\n")

for (name in scenarios) {
	l = compute_models(name)
	
	data = l$data

	folds = l$folds
	for (model in models) {
		output <- (append(output, name))
		output <- (append(output, ","))
		output <- (append(output, print_info(data, folds, l[[model]], model)))
		output <- (append(output, "\n"))

	}
}

for (l in output) {
	cat(l)
}







