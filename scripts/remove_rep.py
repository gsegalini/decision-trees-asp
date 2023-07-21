# Using readlines()

name = "cv.arff"
file1 = open(name, 'r')
lines = file1.readlines()

result = []
for l in lines:
    if (l[0] == "@" or len(l) < 2):
        result.append(l)
        continue
    spl = l.split(",")
    r = int(spl[1])
    if r == 1:
        result.append(l)
	
file1.close()


# writing to file
file1 = open(name, 'w')
file1.writelines(result)
file1.close()
 
