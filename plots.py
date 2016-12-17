#! /usr/bin/python3

import matplotlib.pyplot as plt
import numpy as np

plt.subplot(331)
plt.yscale('linear')
plt.title('sort')
plt.ylabel('delta')
plt.grid(True)

plt.subplot(332)
plt.yscale('linear')
plt.title('request')
plt.grid(True)

plt.subplot(333)
plt.yscale('linear')
plt.title('client')
plt.grid(True)

plt.subplot(334)
plt.yscale('linear')
plt.ylabel('arraySize')
plt.grid(True)

plt.subplot(335)
plt.yscale('linear')
plt.grid(True)

plt.subplot(336)
plt.yscale('linear')
plt.grid(True)

plt.subplot(337)
plt.yscale('linear')
plt.ylabel('requestCount')
plt.grid(True)

plt.subplot(338)
plt.yscale('linear')
plt.grid(True)

plt.subplot(339)
plt.yscale('linear')
plt.grid(True)


arch = "udpPool"
parameters = ["delta", "size", "count"]
types = ["sort", "request", "client"]

for (i, parameter) in zip(range(3), parameters):
    for (j, type) in zip(range(3), types):
        file_name = "results/" + arch + "_" + parameter + "_" + type
        try:
            with open(file_name, 'r') as f:
                print(file_name)
                fromV, toV, stepV = map(lambda x: int(x), f.readline().split())
                f.readline()
                print(fromV, ",", toV, ",", stepV)
                xs = []
                ys = []
                for xi in range(fromV, toV + 1, stepV):
                    xs.append(xi)
                    ys.append(int(f.readline()))
                index = 330 + 3 * i + j + 1
                plt.subplot(index)
                plt.plot(xs, ys)
        except:
            pass


# plot with various axes scales
plt.figure(1)

plt.show()