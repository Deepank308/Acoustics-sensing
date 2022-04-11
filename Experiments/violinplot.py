from turtle import position
import numpy as np
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt

classes = ["No Crack", "At Mid", "At Wall", "At Both"]
labels = []
def add_label(violin, label):
    color = violin["bodies"][0].get_facecolor().flatten()
    labels.append((mpatches.Patch(color=color), label))

with open("cross_ssim_crackpos.txt", "r") as data:
    lines = data.readlines()
    n = len(lines)

    # creating figure and axes to
    # plot the image
    plt.xlim(-1, 4.5)
    # fig, ax = plt.subplots(2, 2)

    i = 0
    for line in lines:
        Y = list(map(float, line.split()))
        add_label(plt.violinplot(Y, positions=[i], showmedians=True), classes[i])
        i = i + 1

    # Function to show the plot
    plt.ylabel("SSIM Index")
    plt.legend(*zip(*labels))
    plt.show()
    # plt.savefig("violinBalls.png", bbox_inches="tight")
