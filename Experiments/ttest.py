from scipy import stats

with open("cross_ssim_crackpos.txt", "r") as data:
    lines = data.readlines()
    n = len(lines)

    i = 0
    ssimvals = []
    original = list(map(float, lines[0].split()))
    for line in lines[1:]:
        vals = list(map(float, line.split()))
        res = stats.ttest_ind(original, vals, equal_var=False)

        print(res)
