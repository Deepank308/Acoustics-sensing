import numpy as np
from operator import add
from scipy import spatial
import matplotlib.pyplot as plt

ball_labels = ["No Ball", "At Front", "At Mid", "At Wall"]
crack_labels = ["No Crack", "At Mid", "At Wall", "At Both"]

def pad_arr(a, orig):
    if a.size > orig.size:
        a = a[:orig.size]
    elif a.size < orig.size:
        a = np.pad(a, (0, orig.size - a.size), 'edge')

    return a

def calc_cosinesim(a, b):
    if len(a) != len(b):
        b = pad_arr(b, a)

    result = 1 - spatial.distance.cosine(a, b)
    return result

def construct_meanvec(filepath = ""):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        meanvec = np.array(list(map(float, lines[0].split())))
        for line in lines[1:]:
            vec = np.array(list(map(float, line.split())))
            meanvec += pad_arr(vec, meanvec)

        meanvec /= len(lines)

    return meanvec

def defect_cosines(orig, a, b, c):
    defect_sims = []

    defect_sims.append(calc_cosinesim(orig, orig))
    defect_sims.append(calc_cosinesim(orig, a))
    defect_sims.append(calc_cosinesim(orig, b))
    defect_sims.append(calc_cosinesim(orig, c))

    return defect_sims

def plot_sims(sims_arr, labels):
    plt.bar(labels, sims_arr)
    plt.xlabel("Defects Variations")
    plt.ylabel("Cosine Similarity")
    plt.show()

def main():
    filepath = "Delays/delay_BallNo.txt"
    orig = construct_meanvec(filepath)

    filepath = "Delays/delay_BallFront.txt"
    ballfront = construct_meanvec(filepath)

    filepath = "Delays/delay_BallMid.txt"
    ballmid = construct_meanvec(filepath)

    filepath = "Delays/delay_BallWall.txt"
    ballwall = construct_meanvec(filepath)

    filepath = "Delays/delay_CrackAll.txt"
    crackall = construct_meanvec(filepath)

    filepath = "Delays/delay_CrackMid.txt"
    crackmid = construct_meanvec(filepath)

    filepath = "Delays/delay_CrackWall.txt"
    crackwall = construct_meanvec(filepath)

    ball_sims = defect_cosines(orig, ballfront, ballmid, ballwall)
    crack_sims = defect_cosines(orig, crackall, crackmid, crackwall)

    plot_sims(ball_sims, ball_labels)
    plot_sims(crack_sims, crack_labels)


if __name__ == '__main__':
    main()