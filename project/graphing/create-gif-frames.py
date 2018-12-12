import matplotlib.pyplot as plt
import numpy as np
import ast, pathlib

"""
This script assumes that you have created an 'out#' folder and have
created a file in it called 'source' that contains the raw array
dumps from the Java code.
"""

OUTPUT = 1

root = 'out{}'.format(OUTPUT)
pathlib.Path(root).mkdir(parents=True, exist_ok=True)

source = '{}/source'.format(root)
file = open(source, 'r')
representations = file.read().split("\n")[:-1]
arrays = [ast.literal_eval(rep) for rep in representations] 

length = len(arrays)
for i in range(length):

    array = arrays[i]

    filename = '{}/{:03d}.png'.format(root, i + 1)
    print('{:03d}/{:03d}'.format(i + 1, length))

    index = np.arange(len(array))
    plt.bar(index, array)
    plt.savefig(filename, dpi=300, bbox_inches='tight')
    plt.clf()
