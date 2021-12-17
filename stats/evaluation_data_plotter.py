import pandas as pd
import matplotlib.pyplot as plt
import scipy as sci
import numpy as np
import seaborn as sns
import matplotlib.ticker as ticker


from evaluation_data_resolver import get_data_frame

REFMERGE = "purple"
GIT = "red"
INTELLIMERGE = "orange"
DISTANCE = 0.3

def set_box_color(bp, color, linestyle):
    plt.setp(bp['boxes'], color=color, linestyle=linestyle)
    plt.setp(bp['whiskers'], color=color, linestyle=linestyle)
    plt.setp(bp['caps'], color=color)
    plt.setp(bp['medians'], color=color)
    
    for patch in bp['boxes']:
        patch.set(facecolor='white')

def wilcoxon_test(x2, x1):
    
    return sci.stats.wilcoxon(x2, y=x1, zero_method='wilcox', correction=False, alternative='two-sided')

def plot_conflicting_scenarios():
    conflicting_ms = get_data_frame('conflicting_scenarios')
    c_projects = conflicting_ms.groupby(['project_name'])

    refMerge_data = conflicting_ms.groupby('project_name')['refMerge_conflicts']
    git_data = conflicting_ms.groupby('project_name')['git_conflicts']
    intelliMerge_data = conflicting_ms.groupby('project_name')['intelliMerge_conflicts']
    refMerge_comments_data = conflicting_ms.groupby('project_name')['refMerge_comments_conflicts']
    git_comments_data = conflicting_ms.groupby('project_name')['git_comments_conflicts']
    intelliMerge_comments_data = conflicting_ms.groupby('project_name')['intelliMerge_comments_conflicts']
    
    git_total = 0
    git_comments_total = 0
    refMerge_total = 0
    refMerge_comments_total = 0
    intelliMerge_total = 0
    intelliMerge_comments_total = 0

    for group in c_projects.groups:
        refMerge = c_projects.get_group(group)['refMerge_conflicts'].iloc[0]
        git = c_projects.get_group(group)['git'].iloc[0]
        intelliMerge = c_projects.get_group(group)['intelliMerge_conflicts'].iloc[0]
        refMerge_comments = c_projects.get_group(group)['refMerge_comments_conflicts'].iloc[0]
        git_comments = c_projects.get_group(group)['git_comments'].iloc[0]
        intelliMerge_comments = c_projects.get_group(group)['intelliMerge_comments'].iloc[0]
        print(group)
        print("RefMerge conflicting Scenarios: " + str(refMerge_comments))
        print("RefMerge conflicting Scenarios Without Comments: " + str(refMerge))
        print("Git conflicting Scenarios: " + str(git_comments))
        print("Git conflicting Scenarios Without Comments: " + str(git))
        print("IntelliMerge conflicting Scenarios: " + str(intelliMerge_comments))
        print("IntelliMerge conflicting Scenarios Without Comments: " + str(intelliMerge))
        git_total += git
        git_comments_total += git_comments
        refMerge_total += refMerge
        refMerge_comments_total += refMerge_comments
        intelliMerge_total += intelliMerge
        intelliMerge_comments_total += intelliMerge_comments
    print("Total")
    print("RefMerge conflicting Scenarios: " + str(refMerge_comments_total))
    print("RefMerge conflicting Scenarios Without Comments: " + str(refMerge_total))
    print("Git conflicting Scenarios: " + str(git_comments_total))
    print("Git conflicting Scenarios Without Comments: " + str(git_total))
    print("IntelliMerge conflicting Scenarios: " + str(intelliMerge_comments_total))
    print("IntelliMerge conflicting Scenarios Without Comments: " + str(intelliMerge_total))


def plot_conflicting_scenarios_bar_graph():
    conflicting_ms = get_data_frame('conflicting_scenarios')
    c_projects = conflicting_ms.groupby(['project_name'])

    git_total = 0
    git_comments_total = 0
    refMerge_total = 0
    refMerge_comments_total = 0
    refMerge_timedout = 0
    refMerge_resolved = 0
    intelliMerge_total = 0
    intelliMerge_comments_total = 0
    intelliMerge_timedout = 0
    intelliMerge_resolved = 0

    for group in c_projects.groups:
        refMerge = c_projects.get_group(group)['refMerge_conflicts'].iloc[0]
        git = c_projects.get_group(group)['git_conflicts'].iloc[0]
        intelliMerge = c_projects.get_group(group)['intelliMerge_conflicts'].iloc[0]
        refMerge_comments = c_projects.get_group(group)['refMerge_comments_conflicts'].iloc[0]
        git_comments = c_projects.get_group(group)['git_comments_conflicts'].iloc[0]
        intelliMerge_comments = c_projects.get_group(group)['intelliMerge_comments_conflicts'].iloc[0]
        refMerge_resolved += c_projects.get_group(group)['refMerge_resolved'].iloc[0]
        intelliMerge_resolved += c_projects.get_group(group)['intelliMerge_resolved'].iloc[0]
        refMerge_timedout += c_projects.get_group(group)['refMerge_timeouts'].iloc[0]
        intelliMerge_timedout += c_projects.get_group(group)['intelliMerge_timeouts'].iloc[0]

        print(group)
        print("RefMerge conflicting Scenarios: " + str(refMerge) + " " + str(refMerge_comments))
        print("RefMerge Timeouts: " + str(c_projects.get_group(group)['refMerge_timeouts'].iloc[0]))
        print("Git conflicting Scenarios: " + str(git_comments))
        print("Git conflicting Scenarios Without Comments: " + str(git))
        print("IntelliMerge conflicting Scenarios: " + str(intelliMerge) + " " + str(intelliMerge_comments))
        print("IntelliMerge Timeouts: " + str(c_projects.get_group(group)['intelliMerge_timeouts'].iloc[0]))
        git_total += git
        git_comments_total += git_comments
        refMerge_total += refMerge
        refMerge_comments_total += refMerge_comments
        intelliMerge_total += intelliMerge
        intelliMerge_comments_total += intelliMerge_comments

    print("Total")
    print("RefMerge conflicting Scenarios: " + str(refMerge_comments_total))
    print("RefMerge conflicting Scenarios Without Comments: " + str(refMerge_total))
    print("RefMerge timeouts: " + str(refMerge_timedout))
    print("Git conflicting Scenarios: " + str(git_comments_total))
    print("Git conflicting Scenarios Without Comments: " + str(git_total))
    print("IntelliMerge conflicting Scenarios: " + str(intelliMerge_comments_total))
    print("IntelliMerge conflicting Scenarios Without Comments: " + str(intelliMerge_total))
    print("IntelliMerge timeouts: " + str(intelliMerge_timedout))

    refMerge_comments_total += refMerge_total
    refMerge_timedout += refMerge_comments_total
    refMerge_resolved += refMerge_timedout

    intelliMerge_comments_total += intelliMerge_total
    intelliMerge_timedout += intelliMerge_comments_total
    intelliMerge_resolved += intelliMerge_timedout

    git_comments_total += git_total


    timedout_ms = [refMerge_timedout, 0, intelliMerge_timedout]
    resolved_ms = [refMerge_resolved,0,intelliMerge_resolved]
    conflicting_comments_ms = [refMerge_comments_total, git_comments_total, intelliMerge_comments_total]
    conflicting_ms = [refMerge_total, git_total, intelliMerge_total]
    labels = ['RefMerge', 'Git', 'IntelliMerge']
    width = 0.5
    fig, ax = plt.subplots()

    ax.bar(labels, resolved_ms,  width,  label="Resolved Merge Scenarios", color="lightgray")
    ax.bar(labels, timedout_ms, width,  label="Timed Out Merge Scenarios", color="darkgray")
    ax.bar(labels, conflicting_comments_ms, width, label="Comment-related Conflicting Merge Scenarios", color="gray")
    ax.bar(labels, conflicting_ms, width, label="Conflicting Merge Scenarios", color="dimgray")

    ax.set_ylabel("Number of Merge Scenarios")
    ax.set_xlabel("Merge Tool")
    ax.legend()
    plt.legend(prop={'size':6})
    plt.ylim([0, 1800])
    plt.savefig('horizontalGraphs/ScenarioStatistics.pdf')


def plot_conflicting_files_with_comments_by_project():
    f = open("results.tex", "a")
    conflicting_files = get_data_frame('conflicting_file_with_comments_stats')
    c_projects = conflicting_files.groupby(['project_name'])

    
    conflicting_files.drop(conflicting_files.index[0])
    fig, ax = plt.subplots(figsize=(15,6))

    conflicting_files.drop(conflicting_files.index[0])


    refMerge_data = conflicting_files.groupby('project_name')['refMerge']
    git_data = conflicting_files.groupby('project_name')['git']
    intelliMerge_data = conflicting_files.groupby('project_name')['intelliMerge']

    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []

    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge']
        git_p = c_projects.get_group(group)['git']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge']
        w1, p1 = wilcoxon_test(refMerge_p, git_p)
        w2, p2 = wilcoxon_test(intelliMerge_p, git_p)
        w3, p3 = wilcoxon_test(refMerge_p, intelliMerge_p)

        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_p))
        print('IntelliMerge Median: ', np.median(intelliMerge_p))
        print('Git Median: ', np.median(git_p))
        print("refMerge, git:", w1, p1)
        print("intelliMerge, git:", w2, p2)
        print("refMerge, intelliMerge:", w3, p3)
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)



    fig, ax = plt.subplots(figsize=(6,14))
    plots = [refMerge_ms, git_ms, intelliMerge_ms]

    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4-0.7, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4+0.7, sym='', widths=0.4)
    set_box_color(bpr, REFMERGE, "--")
    set_box_color(bpg, GIT, "-")
    set_box_color(bpi, INTELLIMERGE, ":")
    ax.set_xlabel('')




    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')

    lgnd = ax.legend([bpr["whiskers"][0], bpg["whiskers"][0], bpi["whiskers"][0]], ["RefMerge", "Git", "IntelliMerge"],
                    loc='lower center', bbox_to_anchor=(0.5, -0.25), frameon=False, fontsize=38, markerscale=100.0)

    plt.xticks(range(0, len(labels)), labels)
    plt.rc('xtick', labelsize=8)

    ax.tick_params(axis='both', which='major', labelsize=32)
    plt.tick_params(axis = "x", which = "both", bottom = False)

    plt.tight_layout()

    print("Project Medians")
    w1, p1 = wilcoxon_test(refMerge_overall, git_overall)
    w2, p2 = wilcoxon_test(intelliMerge_overall, git_overall)
    w3, p3 = wilcoxon_test(refMerge_overall, intelliMerge_overall)
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))
    print("refMerge, git:", w1, p1)
    print("intelliMerge, git:", w2, p2)
    print("refMerge, intelliMerge:", w3, p3)

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))
    f.write("\\newcommand{\\filesRefMergeProjectMedian}{" + str(np.median(refMerge_overall)) + "}\n")
    f.write("\\newcommand{\\filesGitProjectMedian}{" + str(np.median(git_overall)) + "}\n")
    f.write("\\newcommand{\\filesIntelliMergeProjectMedian}{" + str(np.median(intelliMerge_overall)) + "}\n")
    f.write("\\newcommand{\\filesRefMergeScenarioMedian}{" + str(np.median(refMerge_ms)) + "}\n")
    f.write("\\newcommand{\\filesGitScenarioMedian}{" + str(np.median(git_ms)) + "}\n")
    f.write("\\newcommand{\\filesIntelliMergeScenarioMedian}{" + str(np.median(intelliMerge_ms)) + "}\n")

    plt.savefig('horizontalGraphs/FilesPlotTrial.pdf')
    f.close()


def plot_conflicting_files_by_project():
    f = open("results.tex", "a")
    conflicting_files = get_data_frame('conflicting_file_stats')
    c_projects = conflicting_files.groupby(['project_name'])

    
    conflicting_files.drop(conflicting_files.index[0])


    refMerge_data = conflicting_files.groupby('project_name')['refMerge']
    git_data = conflicting_files.groupby('project_name')['git']
    intelliMerge_data = conflicting_files.groupby('project_name')['intelliMerge']

    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []

    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge']
        git_p = c_projects.get_group(group)['git']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge']
        w1, p1 = wilcoxon_test(refMerge_p, git_p)
        w2, p2 = wilcoxon_test(intelliMerge_p, git_p)
        w3, p3 = wilcoxon_test(refMerge_p, intelliMerge_p)
        
        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_p))
        print('IntelliMerge Median: ', np.median(intelliMerge_p))
        print('Git Median: ', np.median(git_p))
        print("refMerge, git:", w1, p1)
        print("intelliMerge, git:", w2, p2)
        print("refMerge, intelliMerge:", w3, p3)
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)



    fig, ax = plt.subplots(figsize=(15,6))
    labels.append('All Merge Scenarios')
    labels.append('All Projects')

    print(len(refMerge))
    print(labels)
    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0-0.5, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0+0.5, sym='', widths=0.4)
    
    set_box_color(bpr, REFMERGE)
    set_box_color(bpg, GIT)
    set_box_color(bpi, INTELLIMERGE)
    ax.set_xlabel("All Merge Scenarios")
    ax.set_ylabel("Number of Conflicting Files")

    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')
    plt.legend()

    plt.xticks(range(0, len(labels) * 2, 2), labels)

    fig.tight_layout()

    print("Project Medians")
    w1, p1 = wilcoxon_test(refMerge_overall, git_overall)
    w2, p2 = wilcoxon_test(intelliMerge_overall, git_overall)
    w3, p3 = wilcoxon_test(refMerge_overall, intelliMerge_overall)
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))
    print("refMerge, git:", w1, p1)
    print("intelliMerge, git:", w2, p2)
    print("refMerge, intelliMerge:", w3, p3)

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))
    f.write("\\newcommand{\\filesRefMergeProjectMedian}{" + str(np.median(refMerge_overall)) + "}\n")
    f.write("\\newcommand{\\filesGitProjectMedian}{" + str(np.median(git_overall)) + "}\n")
    f.write("\\newcommand{\\filesIntelliMergeProjectMedian}{" + str(np.median(intelliMerge_overall)) + "}\n")
    f.write("\\newcommand{\\filesRefMergeScenarioMedian}{" + str(np.median(refMerge_ms)) + "}\n")
    f.write("\\newcommand{\\filesGitScenarioMedian}{" + str(np.median(git_ms)) + "}\n")
    f.write("\\newcommand{\\filesIntelliMergeScenarioMedian}{" + str(np.median(intelliMerge_ms)) + "}\n")

    plt.savefig('horizontalGraphs/FilesPlot.pdf')
    f.close()


def plot_conflicting_loc_per_block_with_comments_stats():
    f = open("results.tex", "a")
    conflicting_loc = get_data_frame('conflicting_loc_per_block_with_comments_stats')
    c_projects = conflicting_loc.groupby(['project_name'])



    conflicting_loc.drop(conflicting_loc.index[0])


    refMerge_data = conflicting_loc[conflicting_loc['refMerge_conflicting_loc'] > -1].groupby('project_name')
    git_data = conflicting_loc.groupby('project_name')['git_conflicting_loc']
    intelliMerge_data = conflicting_loc[conflicting_loc['intelliMerge_conflicting_loc'] > -1].groupby('project_name')


    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []

    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge_conflicting_loc']
        git_p = c_projects.get_group(group)['git_conflicting_loc']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge_conflicting_loc']

        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))
        git_list = list(filter(lambda a: a > -1, git_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_list))
        print('IntelliMerge Median: ', np.median(intelliMerge_list))
        print('Git Median: ', np.median(git_list))
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)

    fig, ax = plt.subplots(figsize=(15,9))
    plots = [refMerge_ms, git_ms, intelliMerge_ms]
    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4-0.7, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4+0.7, sym='', widths=0.4)
    set_box_color(bpr, REFMERGE, "--")
    set_box_color(bpg, GIT, "-")
    set_box_color(bpi, INTELLIMERGE, ":")
    ax.set_xlabel('')




    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')

    lgnd = ax.legend([bpr["whiskers"][0], bpg["whiskers"][0], bpi["whiskers"][0]], ["RefMerge", "Git", "IntelliMerge"],
                    loc='lower center', bbox_to_anchor=(0.5, -0.25), frameon=False, fontsize=38, markerscale=100.0, ncol=3)

    plt.xticks(range(0, len(labels)), labels)
    plt.rc('xtick', labelsize=8)

    ax.tick_params(axis='both', which='major', labelsize=38)
    plt.tick_params(axis = "x", which = "both", bottom = False)

    plt.tight_layout()



    print("Project Medians")
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))

    plt.savefig('horizontalGraphs/LOCPerBlockPlot.pdf')
    f.close()

def plot_conflicting_loc_with_comments_by_project():
    f = open("results.tex", "a")
    conflicting_loc = get_data_frame('conflicting_loc_with_comments_stats')
    c_projects = conflicting_loc.groupby(['project_name'])


    
    conflicting_loc.drop(conflicting_loc.index[0])


    refMerge_data = conflicting_loc[conflicting_loc['refMerge_conflicting_loc'] > -1].groupby('project_name')
    git_data = conflicting_loc.groupby('project_name')['git_conflicting_loc']
    intelliMerge_data = conflicting_loc[conflicting_loc['intelliMerge_conflicting_loc'] > -1].groupby('project_name')


    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []

    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge_conflicting_loc']
        git_p = c_projects.get_group(group)['git_conflicting_loc']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge_conflicting_loc']
        w1, p1 = wilcoxon_test(refMerge_p, git_p)
        w2, p2 = wilcoxon_test(intelliMerge_p, git_p)
        w3, p3 = wilcoxon_test(refMerge_p, intelliMerge_p)

        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_list))
        print('IntelliMerge Median: ', np.median(intelliMerge_list))
        print('Git Median: ', np.median(git_list))
        print("refMerge, git:", w1, p1)
        print("intelliMerge, git:", w2, p2)
        print("refMerge, intelliMerge:", w3, p3)
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)

    fig, ax = plt.subplots(figsize=(6,14))
    plots = [refMerge_ms, git_ms, intelliMerge_ms]

    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4-0.7, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4+0.7, sym='', widths=0.4)

    set_box_color(bpr, REFMERGE, "--")
    set_box_color(bpg, GIT, "-")
    set_box_color(bpi, INTELLIMERGE, ":")
    ax.set_xlabel('')




    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')

    lgnd = ax.legend([bpr["whiskers"][0], bpg["whiskers"][0], bpi["whiskers"][0]], ["RefMerge", "Git", "IntelliMerge"],
                    loc='lower center', bbox_to_anchor=(0.5, -0.25), frameon=False, fontsize=38, markerscale=100.0)

    plt.xticks(range(0, len(labels)), labels)
    plt.rc('xtick', labelsize=8)

    ax.tick_params(axis='both', which='major', labelsize=32)
    plt.tick_params(axis = "x", which = "both", bottom = False)

    plt.tight_layout()

    print("Project Medians")
    w1, p1 = wilcoxon_test(refMerge_overall, git_overall)
    w2, p2 = wilcoxon_test(intelliMerge_overall, git_overall)
    w3, p3 = wilcoxon_test(refMerge_overall, intelliMerge_overall)
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))
    print("refMerge, git:", w1, p1)
    print("intelliMerge, git:", w2, p2)
    print("refMerge, intelliMerge:", w3, p3)

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))
    f.write("\\newcommand{\\locRefMergeProjectMedian}{" + str(np.median(refMerge_overall)) + "}\n")
    f.write("\\newcommand{\\locGitProjectMedian}{" + str(np.median(git_overall)) + "}\n")
    f.write("\\newcommand{\\locIntelliMergeProjectMedian}{" + str(np.median(intelliMerge_overall)) + "}\n")
    f.write("\\newcommand{\\locRefMergeScenarioMedian}{" + str(np.median(refMerge_ms)) + "}\n")
    f.write("\\newcommand{\\locGitScenarioMedian}{" + str(np.median(git_ms)) + "}\n")
    f.write("\\newcommand{\\locIntelliMergeScenarioMedian}{" + str(np.median(intelliMerge_ms)) + "}\n")

    plt.savefig('horizontalGraphs/LOCPlot.pdf')
    f.close()


def plot_conflicting_loc_by_project():
    f = open("results.tex", "a")
    conflicting_loc = get_data_frame('conflicting_loc_stats')
    c_projects = conflicting_loc.groupby(['project_name'])


    
    conflicting_loc.drop(conflicting_loc.index[0])


    refMerge_data = conflicting_loc[conflicting_loc['refMerge_conflicting_loc'] > -1].groupby('project_name')
    git_data = conflicting_loc.groupby('project_name')['git_conflicting_loc']
    intelliMerge_data = conflicting_loc[conflicting_loc['intelliMerge_conflicting_loc'] > -1].groupby('project_name')


    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []

    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge_conflicting_loc']
        git_p = c_projects.get_group(group)['git_conflicting_loc']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge_conflicting_loc']
        w1, p1 = wilcoxon_test(refMerge_p, git_p)
        w2, p2 = wilcoxon_test(intelliMerge_p, git_p)
        w3, p3 = wilcoxon_test(refMerge_p, intelliMerge_p)
        
        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_list))
        print('IntelliMerge Median: ', np.median(intelliMerge_list))
        print('Git Median: ', np.median(git_list))
        print("refMerge, git:", w1, p1)
        print("intelliMerge, git:", w2, p2)
        print("refMerge, intelliMerge:", w3, p3)
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)

    fig, ax = plt.subplots(figsize=(15,6))
    labels.append('All Merge Scenarios')
    labels.append('All Projects')


    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0-0.5, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0+0.5, sym='', widths=0.4)
    
    set_box_color(bpr, REFMERGE)
    set_box_color(bpg, GIT)
    set_box_color(bpi, INTELLIMERGE)
    ax.set_xlabel("All Merge Scenarios")
    ax.set_ylabel("Number of Conflicting LOC")

    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')
    plt.legend()

    plt.xticks(range(0, len(labels) * 2, 2), labels)
    plt.rc('xtick', labelsize=8)
    plt.setp(ax.xaxis.get_majorticklabels(), rotation=0, ha="left", rotation_mode="anchor")

    fig.tight_layout()

    print("Project Medians")
    w1, p1 = wilcoxon_test(refMerge_overall, git_overall)
    w2, p2 = wilcoxon_test(intelliMerge_overall, git_overall)
    w3, p3 = wilcoxon_test(refMerge_overall, intelliMerge_overall)
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))
    print("refMerge, git:", w1, p1)
    print("intelliMerge, git:", w2, p2)
    print("refMerge, intelliMerge:", w3, p3)

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))
    f.write("\\newcommand{\\locRefMergeProjectMedian}{" + str(np.median(refMerge_overall)) + "}\n")
    f.write("\\newcommand{\\locGitProjectMedian}{" + str(np.median(git_overall)) + "}\n")
    f.write("\\newcommand{\\locIntelliMergeProjectMedian}{" + str(np.median(intelliMerge_overall)) + "}\n")
    f.write("\\newcommand{\\locRefMergeScenarioMedian}{" + str(np.median(refMerge_ms)) + "}\n")
    f.write("\\newcommand{\\locGitScenarioMedian}{" + str(np.median(git_ms)) + "}\n")
    f.write("\\newcommand{\\locIntelliMergeScenarioMedian}{" + str(np.median(intelliMerge_ms)) + "}\n")

    plt.savefig('horizontalGraphs/LOCPlot.pdf')
    f.close()

def plot_conflict_blocks_with_comments_by_project():
    f = open("results.tex", "a")
    conflict_blocks = get_data_frame('conflict_block_with_comments_stats')
    c_projects = conflict_blocks.groupby(['project_name'])

    conflict_blocks.drop(conflict_blocks.index[0])

    refMerge_data = conflict_blocks.groupby('project_name')['refMerge_conflict_blocks']
    git_data = conflict_blocks.groupby('project_name')['git_conflict_blocks']
    intelliMerge_data = conflict_blocks.groupby('project_name')['intelliMerge_conflict_blocks']



    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []


    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge_conflict_blocks']
        git_p = c_projects.get_group(group)['git_conflict_blocks']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge_conflict_blocks']
        w1, p1 = wilcoxon_test(refMerge_p, git_p)
        w2, p2 = wilcoxon_test(intelliMerge_p, git_p)
        w3, p3 = wilcoxon_test(refMerge_p, intelliMerge_p)

        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_p))
        print('IntelliMerge Median: ', np.median(intelliMerge_p))
        print('Git Median: ', np.median(git_p))
        print("refMerge, git:", w1, p1)
        print("intelliMerge, git:", w2, p2)
        print("refMerge, intelliMerge:", w3, p3)
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)



    fig, ax = plt.subplots(figsize=(6,14))
    plots = [refMerge_ms, git_ms, intelliMerge_ms]
    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4-0.7, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*1.4+0.7, sym='', widths=0.4)

    set_box_color(bpr, REFMERGE, "--")
    set_box_color(bpg, GIT, "-")
    set_box_color(bpi, INTELLIMERGE, ":")
    ax.set_xlabel('')




    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')

    lgnd = ax.legend([bpr["whiskers"][0], bpg["whiskers"][0], bpi["whiskers"][0]], ["RefMerge", "Git", "IntelliMerge"],
                    loc='lower center', bbox_to_anchor=(0.5, -0.25), frameon=False, fontsize=38, markerscale=100.0)

    plt.xticks(range(0, len(labels)), labels)
    plt.rc('xtick', labelsize=8)

    ax.tick_params(axis='both', which='major', labelsize=32)
    plt.tick_params(axis = "x", which = "both", bottom = False)

    plt.tight_layout()

    print("Project Medians")
    w1, p1 = wilcoxon_test(refMerge_overall, git_overall)
    w2, p2 = wilcoxon_test(intelliMerge_overall, git_overall)
    w3, p3 = wilcoxon_test(refMerge_overall, intelliMerge_overall)
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))
    print("refMerge, git:", w1, p1)
    print("intelliMerge, git:", w2, p2)
    print("refMerge, intelliMerge:", w3, p3)

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))
    f.write("\\newcommand{\\blocksRefMergeProjectMedian}{" + str(np.median(refMerge_overall)) + "}\n")
    f.write("\\newcommand{\\blocksGitProjectMedian}{" + str(np.median(git_overall)) + "}\n")
    f.write("\\newcommand{\\blocksIntelliMergeProjectMedian}{" + str(np.median(intelliMerge_overall)) + "}\n")
    f.write("\\newcommand{\\blocksRefMergeScenarioMedian}{" + str(np.median(refMerge_ms)) + "}\n")
    f.write("\\newcommand{\\blocksGitScenarioMedian}{" + str(np.median(git_ms)) + "}\n")
    f.write("\\newcommand{\\blocksIntelliMergeScenarioMedian}{" + str(np.median(intelliMerge_ms)) + "}\n")

    plt.savefig('horizontalGraphs/BlocksPlot.pdf')
    f.close()


def plot_conflict_blocks_by_project():
    f = open("results.tex", "a")
    conflict_blocks = get_data_frame('conflict_block_stats')
    c_projects = conflict_blocks.groupby(['project_name'])

    
    conflict_blocks.drop(conflict_blocks.index[0])

    refMerge_data = conflict_blocks.groupby('project_name')['refMerge_conflict_blocks']
    git_data = conflict_blocks.groupby('project_name')['git_conflict_blocks']
    intelliMerge_data = conflict_blocks.groupby('project_name')['intelliMerge_conflict_blocks']



    refMerge = []
    git = []
    intelliMerge = []

    refMerge_overall = []
    git_overall = []
    intelliMerge_overall = []

    refMerge_ms = []
    git_ms = []
    intelliMerge_ms = []

    labels = []

    
    for group in c_projects.groups:
        refMerge_p = c_projects.get_group(group)['refMerge_conflict_blocks']
        git_p = c_projects.get_group(group)['git_conflict_blocks']
        intelliMerge_p = c_projects.get_group(group)['intelliMerge_conflict_blocks']
        w1, p1 = wilcoxon_test(refMerge_p, git_p)
        w2, p2 = wilcoxon_test(intelliMerge_p, git_p)
        w3, p3 = wilcoxon_test(refMerge_p, intelliMerge_p)
        
        refMerge_list = refMerge_p.tolist()
        git_list = git_p.tolist()
        intelliMerge_list = intelliMerge_p.tolist()

        refMerge_list = list(filter(lambda a: a > -1, refMerge_list))
        intelliMerge_list = list(filter(lambda a: a > -1, intelliMerge_list))

        print(group)
        print('RefMerge Median: ', np.median(refMerge_p))
        print('IntelliMerge Median: ', np.median(intelliMerge_p))
        print('Git Median: ', np.median(git_p))
        print("refMerge, git:", w1, p1)
        print("intelliMerge, git:", w2, p2)
        print("refMerge, intelliMerge:", w3, p3)
        refMerge.append(refMerge_list)
        git.append(git_list)
        intelliMerge.append(intelliMerge_list)
        labels.append(group)
        refMerge_overall.append(np.median(refMerge_list))
        git_overall.append(np.median(git_list))
        intelliMerge_overall.append(np.median(intelliMerge_list))
        refMerge_ms = refMerge_ms + refMerge_list
        git_ms = git_ms + git_list
        intelliMerge_ms = intelliMerge_ms + intelliMerge_list

    refMerge.append(refMerge_ms)
    refMerge.append(refMerge_overall)
    git.append(git_ms)
    git.append(git_overall)
    intelliMerge.append(intelliMerge_ms)
    intelliMerge.append(intelliMerge_overall)




    fig, ax = plt.subplots(figsize=(15,6))
    labels.append("All Merge Scenarios")
    labels.append("All Projects")

    labels = ['']
    bpr = plt.boxplot(refMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0-0.5, sym='', widths=0.4)
    bpg = plt.boxplot(git_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0, sym='', widths=0.4)
    bpi = plt.boxplot(intelliMerge_ms, patch_artist=True, positions = np.array(range(len(labels)))*2.0+0.5, sym='', widths=0.4)



    set_box_color(bpr, REFMERGE)
    set_box_color(bpg, GIT)
    set_box_color(bpi, INTELLIMERGE)
    ax.set_xlabel("All Merge Scenarios")
    ax.set_ylabel("Number of Conflict Blocks")

    plt.plot([], c=REFMERGE, label="RefMerge")
    plt.plot([], c=GIT, label='Git')
    plt.plot([], c=INTELLIMERGE, label='IntelliMerge')
    plt.legend()

    plt.xticks(range(0, len(labels) * 2, 2), labels)
    plt.rc('xtick', labelsize=8)

    fig.tight_layout()

    print("Project Medians")
    w1, p1 = wilcoxon_test(refMerge_overall, git_overall)
    w2, p2 = wilcoxon_test(intelliMerge_overall, git_overall)
    w3, p3 = wilcoxon_test(refMerge_overall, intelliMerge_overall)
    print('RefMerge Median: ', np.median(refMerge_overall))
    print('IntelliMerge Median: ', np.median(intelliMerge_overall))
    print('Git Median: ', np.median(git_overall))
    print("refMerge, git:", w1, p1)
    print("intelliMerge, git:", w2, p2)
    print("refMerge, intelliMerge:", w3, p3)

    print("All Merge Scenarios")
    print('RefMerge Median: ', np.median(refMerge_ms))
    print('IntelliMerge Median: ', np.median(intelliMerge_ms))
    print('Git Median: ', np.median(git_ms))
    f.write("\\newcommand{\\blocksRefMergeProjectMedian}{" + str(np.median(refMerge_overall)) + "}\n")
    f.write("\\newcommand{\\blocksGitProjectMedian}{" + str(np.median(git_overall)) + "}\n")
    f.write("\\newcommand{\\blocksIntelliMergeProjectMedian}{" + str(np.median(intelliMerge_overall)) + "}\n")
    f.write("\\newcommand{\\blocksRefMergeScenarioMedian}{" + str(np.median(refMerge_ms)) + "}\n")
    f.write("\\newcommand{\\blocksGitScenarioMedian}{" + str(np.median(git_ms)) + "}\n")
    f.write("\\newcommand{\\blocksIntelliMergeScenarioMedian}{" + str(np.median(intelliMerge_ms)) + "}\n")

    plt.savefig('horizontalGraphs/BlocksPlot.pdf')
    f.close()

def plot_discrepancies_between_tools():
    f = open("results.tex", "a")
    df = get_data_frame('discrepancies_between_tools')
    df.block_discrepancy_size = df.block_discrepancy_size.abs()
    df.loc_discrepancy_size = df.loc_discrepancy_size.abs()
    df_mp = df.groupby(['merge_pair'])

    git_refMerge = df_mp.get_group('git-refMerge')
    git_intelliMerge = df_mp.get_group('git-intelliMerge')


    refMerge_total = git_refMerge[(git_refMerge['block_discrepancy_size'] > 0) | (git_refMerge['loc_discrepancy_size'] > 0)]
    intelliMerge_total = git_intelliMerge[(git_intelliMerge['block_discrepancy_size'] > 0) | (git_intelliMerge['loc_discrepancy_size'] > 0)]
    
    print('Git-RefMerge Discrepancies: ' + str(len(refMerge_total)))
    print('Git-IntelliMerge Discrepancies: ' + str(len(intelliMerge_total)))

    refMerge_small = git_refMerge[((git_refMerge['block_discrepancy_size'] > 0) & (git_refMerge['block_discrepancy_size'] < 50)) | ((git_refMerge['block_discrepancy_size'] == 0) & ((git_refMerge['loc_discrepancy_size'] < 50) & (git_refMerge['loc_discrepancy_size'] > 0)))]
    refMerge_medium = git_refMerge[(git_refMerge['block_discrepancy_size'] >= 50) & (git_refMerge['block_discrepancy_size'] < 100) | ((git_refMerge['block_discrepancy_size'] == 0) & ((git_refMerge['loc_discrepancy_size'] < 100) & (git_refMerge['loc_discrepancy_size'] >= 50)))]
    refMerge_large = git_refMerge[(git_refMerge['block_discrepancy_size'] >= 100) | ((git_refMerge['block_discrepancy_size'] == 0) & (git_refMerge['loc_discrepancy_size'] >= 100))]

    print('Git-RefMerge Small: ' + str(len(refMerge_small)))
    print('Git-RefMerge Medium: ' + str(len(refMerge_medium)))
    print('Git-RefMerge Large: ' + str(len(refMerge_large)))

    intelliMerge_small = git_intelliMerge['merge_commit_id'][((git_intelliMerge['block_discrepancy_size'] > 0) & (git_intelliMerge['block_discrepancy_size'] < 50)) | ((git_intelliMerge['block_discrepancy_size'] == 0) & ((git_intelliMerge['loc_discrepancy_size'] < 50) & (git_intelliMerge['loc_discrepancy_size'] > 0)))]
    intelliMerge_medium = git_intelliMerge['merge_commit_id'][(git_intelliMerge['block_discrepancy_size'] >= 50) & (git_intelliMerge['block_discrepancy_size'] < 100) | ((git_intelliMerge['block_discrepancy_size'] == 0) & ((git_intelliMerge['loc_discrepancy_size'] < 100) & (git_intelliMerge['loc_discrepancy_size'] >= 50)))]
    intelliMerge_large = git_intelliMerge['merge_commit_id'][(git_intelliMerge['block_discrepancy_size'] >= 100) | ((git_intelliMerge['block_discrepancy_size'] == 0) & (git_intelliMerge['loc_discrepancy_size'] >= 100))]

    print('Git-IntelliMerge Small: ' + str(len(intelliMerge_small)))
    print('Git-IntelliMerge Medium: ' + str(len(intelliMerge_medium)))
    print('Git-IntelliMerge Large: ' + str(len(intelliMerge_large)))

    print(intelliMerge_small)

    f.close()


plot_functions = [x for x in dir() if x[:5] == 'plot_']
f = open("results.tex", "w+")
f.close()
print('Options available:')
for i in range(len(plot_functions)):
    print(str(i + 1) + '. ' + plot_functions[i])
print(str(len(plot_functions) + 1) + '. Exit')
while True:
    inp = int(input('Choose an option: '))
    if inp < 1 or inp > len(plot_functions):
        break
    locals()[plot_functions[inp - 1]]()




