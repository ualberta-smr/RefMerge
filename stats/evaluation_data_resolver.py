import pandas as pd
from sqlalchemy import create_engine
import matplotlib.pyplot as plt
import numpy as np
import sys
import re
from scipy import stats
from scipy.stats import ranksums
from tabulate import tabulate
import math
import statistics

supported_types = ['RENAME_CLASS', 'MOVE_CLASS', 'MOVE_RENAME_CLASS', 'RENAME_OPERATION', 'MOVE_OPERATION', 'MOVE_AND_RENAME_OPERATION', 'EXTRACT_OPERATION', 'INLINE_OPERATION']

unsupported_types = ['CHANGE_PACKAGE', 'MOVE_ATTRIBUTE', 'RENAME_ATTRIBUTE', 'PULL_UP_OPERATION', 'PUSH_DOWN_OPERATION', 'ADD_PARAMETER', 'CHANGE_PARAMETER_TYPE', 'RENAME_PACKAGE', 'RENAME_PARAMETER', 'PUSH_DOWN_ATTRIBUTE']


def get_db_connection():
    username = 'root'
    password = "password"
    database_name = 'refMerge_evaluation'
    server = '127.0.0.1'

    return create_engine('mysql+pymysql://{}:{}@{}/{}'.format(username, password, server, database_name))


def regions_intersect(region_1_start, region_1_length, region_2_start, region_2_length):
    if region_1_start + region_1_length < region_2_start:
        return False
    elif region_2_start + region_2_length < region_1_start:
        return False
    return True


def record_involved(x):
    is_source = (x['type'] == 's' and
                 x['old_path'] == x['path'] and
                 regions_intersect(x['old_start_line'], x['old_length'], x['start_line'], x['length']))
    is_dest = (x['type'] == 'd' and
               x['new_path'] == x['path'] and
               regions_intersect(x['new_start_line'], x['new_length'], x['start_line'], x['length']))
    if is_source or is_dest:
        project_url = get_project_url(x['merge_commit_id'])
        merge_commit_hash = get_merge_commit_hash(x['merge_commit_id'])
        write_to_csv(project_url, merge_commit_hash)
    return is_source or is_dest


accepted_types = ['Change Package', 'Extract And Move Method', 'Extract Interface', 'Extract Method',
                      'Extract Superclass', 'Inline Method', 'Move And Rename Class', 'Move Attribute', 'Move Class',
                      'Move Method', 'Pull Up Attribute', 'Pull Up Method', 'Pull Up Method', 'Push Down Method',
                      'Rename Class', 'Rename Method']

covered_types = ['Extract Method', 'Inline Method', 'Rename Method', 'Rename Class', 'Move Method', 'Move Class', 'Move And Rename Method', 'Move And Rename Class']



def write_to_csv(project_url, merge_commit_hash):
    f = open('intelliMerge_data', 'r')
    lines = f.read()
    f.close()
    with open('intelliMerge_data', 'a') as open_file:
        line = []
        line.append(project_url)
        line.append(merge_commit_hash)
        entry = ";".join(line)
        found = False
    
        if merge_commit_hash in lines:
            return
        open_file.write(entry + '\n') 

def write_to_evaluation_csv(entry):
    with open('refMerge_evaluation_commits', 'a') as f:
        f.write(entry + '\n')

def get_refactoring_types_sql_condition():
    type_condition = str()
    for ref_type in unsupported_types:
        type_condition += 'refactoring_type = \"{}\" or '.format(ref_type)
    return type_condition[:-4]


def read_sql_table(table):
    print('Reading table {} from the database'.format(table))
    query = 'SELECT * FROM ' + table
    df = pd.read_sql(query, get_db_connection())
    return df

def get_project_id_by_url(project_url):
    query = "SELECT * FROM project WHERE url = '{}'".format(project_url)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['id']

def get_merge_results():
    return read_sql_table('merge_result')


def get_project_by_id(project_id):
    query = "SELECT * FROM project WHERE id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['name']

def get_project_url(merge_commit_id):
    query = "SELECT * FROM merge_commit WHERE id = '{}'".format(merge_commit_id)
    df = pd.read_sql(query, get_db_connection())
    project_id = df.iloc[0]['project_id']
    query = "SELECT * FROM project WHERE id ='{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['url']

def get_merge_commit_hash(merge_commit_id):
    query = "SELECT * FROM merge_commit WHERE id = '{}'".format(merge_commit_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['commit_hash']


def get_projects():
    query = "SELECT * FROM project"
    df = pd.read_sql(query, get_db_connection())
    return df

def get_conflicting_merge_commits(project_id):
    query = "SELECT * FROM merge_commit WHERE is_conflicting = 1 AND project_id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df

def get_conflict_blocks():
    return read_sql_table('conflict_block') 


def get_conflicting_files():
    return read_sql_table('conflicting_file')

def get_supported_merge_commits():
    query = "SELECT * FROM merge_commit WHERE id not in (SELECT merge_commit_id FROM refactoring WHERE ({}))"\
            .format(get_refactoring_types_sql_condition())
    df = pd.read_sql(query, get_db_connection())
    return df

def get_merge_commits(project_id):
    query = "SELECT * FROM merge_commit WHERE project_id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df

def get_merge_commits():
    return read_sql_table('merge_commit')

def get_conflicting_regions():
    return read_sql_table('conflicting_region')


def get_conflicting_region_histories():
    return read_sql_table('conflicting_region_history')

def mc_has_supported_types(mc_id):
    query = "SELECT distinct refactoring_type FROM refactoring WHERE merge_commit_id = '{}'".format(mc_id)
    df = pd.read_sql(query, get_db_connection())
    for row in df.iloc:
        if row['refactoring_type'] in supported_types:
            return True
    return False

def get_refactorings():
    return read_sql_table('refactoring')

def get_refactoring_regions():
    return read_sql_table('refactoring_region')


def get_accepted_refactoring_regions():
    print('Reading table refactoring_region from the database')

    query = 'select * from refactoring_region where refactoring_id in (select id from refactoring where ({}))'\
        .format(get_refactoring_types_sql_condition())
    return pd.read_sql(query, get_db_connection())


def get_merge_conflict_and_conflicting_loc_diffs_per_tool():
    return
    
def get_statistics_per_project():

    merge_results = get_merge_results()
    counter = 0

    total_precision = {'Git': 0.0, 'IntelliMerge': 0.0, 'RefMerge': 0.0}
    total_recall = {'Git': 0.0, 'IntelliMerge': 0.0, 'RefMerge': 0.0}
    total_conflicts = {'Git': 0, 'IntelliMerge': 0, 'RefMerge': 0}
    total_conflicting_loc = {'Git': 0, 'IntelliMerge': 0, 'RefMerge': 0}

    plot_df = pd.DataFrame(columns=['project_name', 'merge_tool', 'precision', 'recall', 'conflict_blocks', 'conflicting_loc'])
    for project_id, project_mr in merge_results.groupby('project_id'):
        if project_id == 10:
            continue
        counter += 1
        print('Processing project {}'.format(counter))
        project_mr_mc = project_mr.groupby('merge_tool')
        for merge_tool, mr_mc in project_mr_mc:
            conflict_blocks = 0
            conflicting_loc = 0
            auto_merged_precision = 0.0
            auto_merged_recall = 0.0
            all_precision = mr_mc['auto_merged_precision']
            all_recall = mr_mc['auto_merged_recall']
            all_conflicts = mr_mc['total_conflicts']
            all_conflicting_loc = mr_mc['total_conflicting_loc']
            for precision in all_precision:
                if precision > 0:
                    auto_merged_precision = auto_merged_precision + precision
            for recall in all_recall:
                if recall > 0:
                    auto_merged_recall = auto_merged_recall + recall
            for conflicts in all_conflicts:
                if conflicts > 0:
                    conflict_blocks = conflict_blocks + conflicts
            for loc in all_conflicting_loc:
                if loc > 0:
                    conflicting_loc = conflicting_loc + loc

            auto_merged_precision = auto_merged_precision / len(all_precision)
            auto_merged_recall = auto_merged_recall / len(all_recall)
            print(total_precision)
            total_precision[merge_tool] = total_precision[merge_tool] + auto_merged_precision
            total_recall[merge_tool] = total_recall[merge_tool] + auto_merged_recall
            total_conflicts[merge_tool] = total_conflicts[merge_tool] + conflict_blocks
            total_conflicting_loc[merge_tool] = total_conflicting_loc[merge_tool] + conflicting_loc
            plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'merge_tool': merge_tool, 'precision': auto_merged_precision, 'recall': auto_merged_recall, 'conflict_blocks': conflict_blocks, 'conflicting_loc': conflicting_loc}, ignore_index=True)
        
    for k in total_precision:
        avg_precision = total_precision[k] / counter
        avg_recall = total_recall[k] / counter
        plot_df = plot_df.append({'project_name': 'Overall', 'merge_tool': k, 'precision': avg_precision, 'recall': avg_recall, 'conflict_blocks': total_conflicts[k], 'conflicting_loc': total_conflicting_loc[k]}, ignore_index=True)
        

    return plot_df

def get_statistics_per_scenario():

    merge_results = get_merge_results()
    counter = 0

    plot_df = pd.DataFrame(columns=['project_id', 'merge_commit_id', 'merge_tool', 'precision', 'recall', 'conflict_blocks', 'conflicting_loc'])
    mr_grouped_by_mc = merge_results.groupby('merge_commit_id')
    merge_results_df = pd.DataFrame(merge_results)

    for _, mr in merge_results_df.groupby('id'):
        plot_df = plot_df.append({'project_id': mr['project_id'], 'merge_commit_id': mr['merge_commit_id'], 'merge_tool': str(mr['merge_tool'].iloc[0]), 'precision': mr['auto_merged_precision'].iloc[0], 'recall': mr['auto_merged_recall'].iloc[0], 'conflict_blocks': mr['total_conflicts'], 'conflicting_loc': mr['total_conflicting_loc'].iloc[0]}, ignore_index=True)
        
    return plot_df

def get_conflicting_loc_per_block_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])

    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflicting_loc = cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_loc'].tolist()
            intelliMerge_conflicting_loc = cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_loc'].tolist()
            git_conflicting_loc = cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_loc'].tolist()

            for loc in refMerge_conflicting_loc:
                if 'RefMerge' in skip:
                    break
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': -1}, ignore_index = True)
            if len(refMerge_conflicting_loc) == 0 and 'RefMerge' not in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': 0, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': -1}, ignore_index = True)

            for loc in intelliMerge_conflicting_loc:
                if 'IntelliMerge' in skip:
                    break
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': loc, 'git_conflicting_loc': -1}, ignore_index = True)
            if len(intelliMerge_conflicting_loc) == 0 and 'RefMerge' not in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': 0, 'git_conflicting_loc': -1}, ignore_index = True)

            for loc in git_conflicting_loc:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': loc}, ignore_index = True)


#             if 'RefMerge' in skip:
#                 plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)
#
#             elif 'IntelliMerge' in skip:
#                 plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)
#
#             else:
#                 plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)


    return plot_df


def get_conflicting_loc_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = [] 
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_loc'].tolist())
            intelliMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_loc'].tolist())
            git_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_loc'].tolist())

            refMerge_reduction = (git_conflicting_loc - refMerge_conflicting_loc) * 100 / git_conflicting_loc
            intelliMerge_reduction = (git_conflicting_loc - intelliMerge_conflicting_loc) * 100 / git_conflicting_loc
            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

        refMerge_total = refMerge_total / count
        intelliMerge_total = intelliMerge_total / count
        print(project_id, " RefMerge: ", refMerge_total)
        print(project_id, " IntelliMerge: ", intelliMerge_total)

    return plot_df


def get_conflicting_loc_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = [] 
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            intelliMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            git_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            if git_conflicting_loc == 0:
                continue

            refMerge_reduction = (git_conflicting_loc - refMerge_conflicting_loc) * 100 / git_conflicting_loc
            intelliMerge_reduction = (git_conflicting_loc - intelliMerge_conflicting_loc) * 100 / git_conflicting_loc

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)



            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

        refMerge_total = refMerge_total / count
        intelliMerge_total = intelliMerge_total / count
        print(project_id, " RefMerge: ", refMerge_total)
        print(project_id, " IntelliMerge: ", intelliMerge_total)

    return plot_df

def get_conflicting_scenarios():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicts', 'refMerge_timeouts', 'refMerge_resolved', 'intelliMerge_conflicts', 'intelliMerge_timeouts', 'intelliMerge_resolved' 'git_conflicts', 'refMerge_comments_conflicts', 'git_comments_conflicts', 'intelliMerge_comments_conflicts'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    refMerge_final = 0
    intelliMerge_final = 0
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_comments_total = 0
        intelliMerge_comments_total = 0
        refMerge_total = 0
        intelliMerge_total = 0
        refMerge_resolved = 0
        intelliMerge_resolved = 0
        refMerge_timeouts = 0
        intelliMerge_timeouts = 0
        git_total = 0
        git_comments_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            if mc_cb['is_done'] == 0:
                continue
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])

            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])

            if len(skip) == 2:
                refMerge_conflict_blocks_comments = 0
                refMerge_conflict_blocks = 0
                intelliMerge_conflict_blocks_comments = 0
                intelliMerge_conflict_blocks = 0
                git_conflict_blocks = 1
            else:
                cb_group = cb_mc.get_group(mc_cb['id']) 
                refMerge_conflict_blocks_comments = len(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['id'].tolist())
                intelliMerge_conflict_blocks_comments = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['id'].tolist())
                refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
                intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
                git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if 'RefMerge' in skip:
                refMerge_timeouts += 1
            elif refMerge_conflict_blocks != 0:
                refMerge_total += 1
            elif refMerge_conflict_blocks_comments != 0:
                refMerge_comments_total += 1
            else:
                refMerge_resolved += 1

            if 'IntelliMerge' in skip:
                intelliMerge_timeouts += 1
            elif intelliMerge_conflict_blocks != 0:
                intelliMerge_total += 1
            elif intelliMerge_conflict_blocks_comments != 0:
                intelliMerge_comments_total += 1
            else: 
                intelliMerge_resolved += 1

            if git_conflict_blocks != 0:
                git_total += 1
            else:
                git_comments_total += 1
            count += 1
        plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicts': refMerge_total, 'refMerge_timeouts': refMerge_timeouts, 'refMerge_resolved': refMerge_resolved, 'intelliMerge_conflicts': intelliMerge_total, 'intelliMerge_timeouts': intelliMerge_timeouts, 'intelliMerge_resolved': intelliMerge_resolved, 'git_conflicts': git_total, 'refMerge_comments_conflicts': refMerge_comments_total, 'intelliMerge_comments_conflicts': intelliMerge_comments_total, 'git_comments_conflicts': git_comments_total}, ignore_index = True)
        
    return plot_df

def get_conflicting_scenarios_with_comments():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    refMerge_final = 0
    intelliMerge_final = 0
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            if mc_cb['is_done'] == 0:
                continue;
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if git_conflict_blocks == 0:
                continue
            if 'RefMerge' in skip or refMerge_conflict_blocks != 0:
                refMerge_total += 1

            if 'IntelliMerge' in skip or intelliMerge_conflict_blocks != 0:
                intelliMerge_total += 1
            count += 1

        refMerge_reduction = (count - refMerge_total) * 100 / count
        intelliMerge_reduction = (count - intelliMerge_total) * 100 / count
        plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction, 'intelliMerge': intelliMerge_reduction}, ignore_index = True)
        refMerge_final += refMerge_total
        intelliMerge_final += intelliMerge_total
        print(refMerge_final)
        print(intelliMerge_final)
        
    return plot_df

def get_conflicting_scenario_reduction():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    refMerge_final = 0
    intelliMerge_final = 0
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            if mc_cb['is_done'] == 0:
                continue;
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) < 0:
                    skip.append(mr['merge_tool'])

            if len(skip) == 2:
                continue

            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if git_conflict_blocks == 0:
                continue
            if 'RefMerge' in skip or refMerge_conflict_blocks != 0:
                refMerge_total += 1

            if 'IntelliMerge' in skip or intelliMerge_conflict_blocks != 0:
                intelliMerge_total += 1
            count += 1

        refMerge_reduction = (count - refMerge_total) * 100 / count
        intelliMerge_reduction = (count - intelliMerge_total) * 100 / count
        plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction, 'intelliMerge': intelliMerge_reduction}, ignore_index = True)
        refMerge_final += refMerge_total
        intelliMerge_final += intelliMerge_total
        print(refMerge_final)
        print(intelliMerge_final)
        
    return plot_df

def get_discrepancies_between_tools():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'merge_commit_id', 'merge_pair', 'block_discrepancy_size', 'loc_discrepancy_size'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        for mc_cb in project_mc.iloc:
            if mc_cb['is_done'] == 0:
                continue
            cb_group = cb_mc.get_group(mc_cb['id'])

            refMerge_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['id'].tolist())
            intelliMerge_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['id'].tolist())
            git_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git')]['id'].tolist())
            refMerge_lines = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_loc'].tolist())
            intelliMerge_lines = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_loc'].tolist())
            git_lines = sum(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_loc'].tolist())

            git_refMerge_blocks = (git_blocks - refMerge_blocks) * 100 / git_blocks 
            git_refMerge_lines = (git_lines - refMerge_lines) * 100 / git_lines
            git_intelliMerge_blocks = (git_blocks - intelliMerge_blocks) * 100 / git_blocks
            git_intelliMerge_lines = (git_lines - intelliMerge_lines) * 100 / git_lines

            plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'merge_commit_id': mc_cb['id'], 'merge_pair': 'git-refMerge', 'block_discrepancy_size': git_refMerge_blocks, 'loc_discrepancy_size': git_refMerge_lines}, ignore_index = True)
            plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'merge_commit_id': mc_cb['id'], 'merge_pair': 'git-intelliMerge', 'block_discrepancy_size': git_intelliMerge_blocks, 'loc_discrepancy_size': git_intelliMerge_lines}, ignore_index = True)
    return plot_df


def get_conflicting_loc_reduction():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_loc'].tolist())
            intelliMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_loc'].tolist())
            git_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_loc'].tolist())
            if git_conflicting_loc == 0:
                continue

            refMerge_reduction = (git_conflicting_loc - refMerge_conflicting_loc) * 100 / git_conflicting_loc
            intelliMerge_reduction = (git_conflicting_loc - intelliMerge_conflicting_loc) * 100 / git_conflicting_loc

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'intelliMerge': intelliMerge_reduction}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction, 'intelliMerge': intelliMerge_reduction}, ignore_index = True)

            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction


    return plot_df


def get_conflict_block_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflict_blocks', 'intelliMerge_conflict_blocks', 'git_conflict_blocks'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git')]['id'].tolist())

            refMerge_reduction = (git_conflict_blocks - refMerge_conflict_blocks) * 100 / git_conflict_blocks
            intelliMerge_reduction = (git_conflict_blocks - intelliMerge_conflict_blocks) * 100 / git_conflict_blocks
            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': -1, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': -1, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

        refMerge_total = refMerge_total / count
        intelliMerge_total = intelliMerge_total / count
        print(project_id, " RefMerge: ", refMerge_total)
        print(project_id, " IntelliMerge: ", intelliMerge_total)

    return plot_df

def get_conflict_block_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflict_blocks', 'intelliMerge_conflict_blocks', 'git_conflict_blocks'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        
        
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if git_conflict_blocks == 0:
                continue

            refMerge_reduction = (git_conflict_blocks - refMerge_conflict_blocks) * 100 / git_conflict_blocks
            intelliMerge_reduction = (git_conflict_blocks - intelliMerge_conflict_blocks) * 100 / git_conflict_blocks

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': -1, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': -1, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)




            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

    return plot_df

def get_conflict_block_reduction():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        
        
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            

            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git')]['id'].tolist())
            if git_conflict_blocks == 0:
                continue

            refMerge_reduction = (git_conflict_blocks - refMerge_conflict_blocks) * 100 / git_conflict_blocks
            intelliMerge_reduction = (git_conflict_blocks - intelliMerge_conflict_blocks) * 100 / git_conflict_blocks
            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'intelliMerge': intelliMerge_reduction}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction, 'intelliMerge': intelliMerge_reduction}, ignore_index = True)


            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

    return plot_df


def get_conflicting_scenario_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge', 'git'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        
        
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = 0
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip += 1
            if skip == 2:
                continue

            cb_group = cb_mc.get_group(mc_cb['id'])

            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())

            if refMerge_conflict_blocks > 0:
                refMerge_conflict_blocks = 1
            if intelliMerge_conflict_blocks > 0:
                intelliMerge_conflict_blocks = 1
            if git_conflict_blocks > 0:
                git_conflict_blocks = 1

            plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflict_blocks, 'intelliMerge': intelliMerge_conflict_blocks, 'git': git_conflict_blocks}, ignore_index = True)


    return plot_df

def get_conflicting_file_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'git', 'intelliMerge'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;
            skip = []
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_file_id'].tolist()))
            intelliMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_file_id'].tolist()))
            git_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_file_id'].tolist()))


            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': -1, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': -1}, ignore_index=True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)

            


    return plot_df

def get_conflicting_file_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'git', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))
            intelliMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))
            git_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': -1, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': -1}, ignore_index=True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)


            


    return plot_df

def get_conflicting_file_reduction():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_file_id'].tolist()))
            intelliMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_file_id'].tolist()))
            git_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_file_id'].tolist()))
            if git_conflicting_files == 0:
                continue
            refMerge_reduction = (git_conflicting_files - refMerge_conflicting_files) * 100 / git_conflicting_files
            intelliMerge_reduction = (git_conflicting_files - intelliMerge_conflicting_files) * 100 / git_conflicting_files

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'intelliMerge': intelliMerge_reduction}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction, 'intelliMerge': intelliMerge_reduction}, ignore_index = True)

            


    return plot_df


def get_conflicting_stats():

    merge_results = get_merge_results()
    counter = 0

    plot_df = pd.DataFrame(columns=['project_name', 'merge_commit_id', 'refMerge_loc_diff', 'refMerge_block_diff', 'intelliMerge_loc_diff', 'intelliMerge_block_diff'])
    for project_id, project_mr in merge_results.groupby('project_id'):
        loc_diff = {'IntelliMerge': 0.0, 'RefMerge': 0.0}
        block_diff = {'IntelliMerge': 0.0, 'RefMerge': 0.0}
        file_diff = {'IntelliMerge': 0, 'RefMerge': 0}
        conf_scenario_diff = {'IntelliMerge': 0, 'RefMerge': 0}
        blocks = {'Git': 0, 'IntelliMerge': 0, 'RefMerge': 0}
        loc = {'Git': 0, 'IntelliMerge': 0, 'RefMerge': 0}
        counter += 1
        print("-----------------------------------------------")
        print('Processing project {}'.format(counter), ": ", get_project_by_id(project_id))
        timeout = False
        project_mr_mc = project_mr.groupby('merge_commit_id')
        count = 0
        for merge_tool, mr_mc in project_mr_mc:
            conflict_blocks = 0
            conflicting_loc = 0
            if int(mr_mc['runtime'].iloc[0]) > 1799999 or int(mr_mc['runtime'].iloc[1]) > 1799999 or int(mr_mc['runtime'].iloc[2]) > 1799999:
                continue
            
            mc = mr_mc['merge_commit_id'].iloc[0]


            count += 1
            blocks[mr_mc['merge_tool'].iloc[0]] = mr_mc['total_conflicts'].iloc[0]
            loc[mr_mc['merge_tool'].iloc[0]] = mr_mc['total_conflicting_loc'].iloc[0]
            blocks[mr_mc['merge_tool'].iloc[1]] = mr_mc['total_conflicts'].iloc[1]
            loc[mr_mc['merge_tool'].iloc[1]] = mr_mc['total_conflicting_loc'].iloc[1]
            blocks[mr_mc['merge_tool'].iloc[2]] = mr_mc['total_conflicts'].iloc[2]
            loc[mr_mc['merge_tool'].iloc[2]] = mr_mc['total_conflicting_loc'].iloc[2]
            
            git_loc = loc['Git']
            git_block = blocks['Git']
            refMerge_loc_diff = (git_loc - loc['RefMerge']) / git_loc
            refMerge_block_diff = (git_block - blocks['RefMerge']) / git_block
            intelliMerge_loc_diff = (git_loc - loc['IntelliMerge']) / git_loc
            intelliMerge_block_diff = (git_block - blocks['IntelliMerge']) / git_block

            plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'merge_commit_id': mr_mc['merge_commit_id'].iloc[0], 'refMerge_loc_diff': refMerge_loc_diff, 'refMerge_block_diff': refMerge_block_diff, 'intelliMerge_loc_diff': intelliMerge_loc_diff, 'intelliMerge_block_diff': intelliMerge_block_diff}, ignore_index=True)
            loc_diff['RefMerge'] += refMerge_loc_diff
            loc_diff['IntelliMerge'] += intelliMerge_loc_diff
            block_diff['RefMerge'] += refMerge_block_diff
            block_diff['IntelliMerge'] += intelliMerge_block_diff

        loc_diff['RefMerge'] = loc_diff['RefMerge'] * 100 / count
        loc_diff['IntelliMerge'] = loc_diff['IntelliMerge'] * 100 / count
        block_diff['RefMerge'] = block_diff['RefMerge'] * 100 / count
        block_diff['IntelliMerge'] = block_diff['IntelliMerge'] * 100 / count

        print("RefMerge Block Diff for project ", counter, ": ", block_diff['RefMerge'])
        print("IntelliMerge Block Diff for project ", counter, ": ", block_diff['IntelliMerge'])
        print("RefMerge LOC Diff for project ", counter, ": ", loc_diff['RefMerge'])
        print("IntelliMerge LOC Diff for project ", counter, ": ", loc_diff['IntelliMerge'])


    return plot_df


def get_detailed_scenario_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'total_resolvedIntelliMerge', 'timedOut_intelliMerge', 'total_conflictingIntelliMerge', 'total_resolvedRefMerge', 'timedOut_refMerge', 'total_conflictingRefMerge'])


    all_intelliMerge_resolved = 0
    all_intelliMerge_timeouts = 0
    all_intelliMerge_same = 0
    all_refMerge_resolved = 0
    all_refMerge_timeouts = 0
    all_refMerge_same = 0

    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios
        total_resolved_intelliMerge = len(project_data[(project_data.merge_tool=="IntelliMerge") & (project_data.total_conflicts == 0) & (project_data.runtime != "-1")])
        total_resolved_refMerge = len(project_data[(project_data.merge_tool=="RefMerge") & (project_data.total_conflicts == 0) & (project_data.runtime != "-1")])

        total_timedOut_intelliMerge = len(project_data[(project_data.merge_tool=="IntelliMerge") & (project_data.runtime == "-1")])
        total_timedOut_refMerge = len(project_data[(project_data.merge_tool=="RefMerge") & (project_data.runtime == "-1")])

        total_conflicting_intelliMerge = total_scenarios - total_resolved_intelliMerge - total_timedOut_intelliMerge
        total_conflicting_refMerge = total_scenarios - total_resolved_refMerge - total_timedOut_refMerge

        all_intelliMerge_resolved += total_resolved_intelliMerge
        all_refMerge_resolved += total_resolved_refMerge

        all_intelliMerge_timeouts += total_timedOut_intelliMerge
        all_refMerge_timeouts += total_timedOut_refMerge

        all_intelliMerge_same += total_conflicting_intelliMerge
        all_refMerge_same += total_conflicting_refMerge

        #print("median reduction: " , statistics.median(intellimerge_reductions))
        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'total_resolvedIntelliMerge':total_resolved_intelliMerge,
            'timedOut_intelliMerge': total_timedOut_intelliMerge,
            'total_conflictingIntelliMerge': total_conflicting_intelliMerge,
            'total_resolvedRefMerge':total_resolved_refMerge,
            'timedOut_refMerge': total_timedOut_refMerge,
            'total_conflictingRefMerge': total_conflicting_refMerge}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'total_resolvedIntelliMerge':all_intelliMerge_resolved,
            'total_conflictingIntelliMerge': all_intelliMerge_same,
            'timedOut_intelliMerge': all_intelliMerge_timeouts,
            'total_resolvedRefMerge':all_refMerge_resolved,
            'timedOut_refMerge': all_refMerge_timeouts,
            'total_conflictingRefMerge': all_refMerge_same}, ignore_index=True)
    df.to_csv("../results/overall_scenarios.csv")


def get_detailed_file_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflicting_files_intelliMerge', 'increased_conflicting_files_intelliMerge', 'same_conflicting_files_intelliMerge', 'reduced_conflicting_files_refMerge', 'increased_conflicting_files_refMerge', 'same_conflicting_files_refMerge',])

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intelliMerge_reductions = []
        intelliMerge_increases = []
        refMerge_reductions = []
        refMerge_increases = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]
            intelliMerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]
            refMerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario) , 'total_conflicting_files'].values[0]

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increases.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increases.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increases)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increases)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increases) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increases)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increases) > 0):
            median_refMerge_increase = statistics.median(refMerge_increases)

        #print("median reduction: " , statistics.median(intellimerge_reductions))
        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'reduced_conflicting_files_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflicting_files_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflicting_files_intelliMerge':same_intelliMerge_git,
            'reduced_conflicting_files_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflicting_files_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflicting_files_refMerge':same_refMerge_git}, ignore_index=True)


    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'reduced_conflicting_files_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflicting_files_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflicting_files_intelliMerge':-1,
            'reduced_conflicting_files_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflicting_files_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflicting_files_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_files.csv")


def get_detailed_block_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflict_intelliMerge', 'increased_conflict_intelliMerge', 'same_conflict_intelliMerge', 'reduced_conflict_refMerge', 'increased_conflict_refMerge', 'same_conflict_refMerge',])

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intelliMerge_reductions = []
        intelliMerge_increase = []
        refMerge_reductions = []
        refMerge_increase = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            intelliMerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            refMerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increase.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increase.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increase)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increase)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increase) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increase)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increase) > 0):
            median_refMerge_increase = statistics.median(refMerge_increase)

        #print("median reduction: " , statistics.median(intellimerge_reductions))
        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'reduced_conflict_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflict_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflict_intelliMerge':same_intelliMerge_git,
            'reduced_conflict_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflict_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflict_refMerge':same_refMerge_git}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'reduced_conflict_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflict_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflict_intelliMerge':-1,
            'reduced_conflict_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflict_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflict_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_blocks.csv")

def get_detailed_loc_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflicting_loc_intelliMerge', 'increased_conflicting_loc_intelliMerge', 'same_conflict_intelliMerge', 'reduced_conflicting_loc_refMerge', 'increased_conflicting_loc_refMerge', 'same_conflict_refMerge',])

    counter = 0

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intelliMerge_reductions = []
        intelliMerge_increase = []
        refMerge_reductions = []
        refMerge_increase = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]
            intelliMerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]
            refMerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increase.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increase.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increase)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increase)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increase) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increase)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increase) > 0):
            median_refMerge_increase = statistics.median(refMerge_increase)

        #print("median reduction: " , statistics.median(intellimerge_reductions))
        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'reduced_conflicting_loc_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflicting_loc_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflict_intelliMerge':same_intelliMerge_git,
            'reduced_conflicting_loc_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflicting_loc_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflict_refMerge':same_refMerge_git}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'reduced_conflicting_loc_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflicting_loc_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflict_intelliMerge':-1,
            'reduced_conflicting_loc_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflicting_loc_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflict_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_loc.csv")



def get_detailed_stats_per_proj():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflicting_loc_intelliMerge', 'increased_conflicting_loc_intelliMerge', 'same_conflict_intelliMerge', 'reduced_conflicting_loc_refMerge', 'increased_conflicting_loc_refMerge' 'same_conflict_refMerge',])

    counter = 0
    all_intellimerge_reductions = []
    all_intellimerge_increase = []
    all_refmerge_reductions = []
    all_refmerge_increase = []
    all_intellimerge_resolved = 0
    all_refmerge_resolved = 0
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intellimerge_reductions = []
        intellimerge_increase = []
        refmerge_reductions = []
        refmerge_increase = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios
        total_resolved_intellimerge = len(project_data[(project_data.merge_tool=="IntelliMerge") & (project_data.total_conflicts == 0)])
        total_resolved_refmerge = len(project_data[(project_data.merge_tool=="RefMerge") & (project_data.total_conflicts == 0)])

        all_intellimerge_resolved += total_resolved_intellimerge
        all_refmerge_resolved += total_resolved_refmerge

        reduced_intellimerge_git = 0
        increased_intellimerge_git = 0
        same_intellimerge_git = 0

        reduced_refmerge_git = 0
        increased_refmerge_git = 0
        same_refmerge_git = 0

        median_intellimerge_reduction = 0
        median_refmerge_reduction = 0
        median_intellimerge_increase = 0
        median_refmerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            intellimerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            refmerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]

            if intellimerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intellimerge_conflicts:
                    same_intellimerge_git += 1
                elif intellimerge_conflicts < git_conflicts:
                    reduced_intellimerge_git +=1
                    intellimerge_reductions.append((git_conflicts - intellimerge_conflicts)/git_conflicts)
                else:
                    increased_intellimerge_git += 1
                    intellimerge_increase.append((intellimerge_conflicts - git_conflicts)/git_conflicts)

            if refmerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refmerge_conflicts:
                    same_refmerge_git += 1
                elif refmerge_conflicts < git_conflicts:
                    reduced_refmerge_git +=1
                    refmerge_reductions.append((git_conflicts - refmerge_conflicts)/git_conflicts)
                else:
                    increased_refmerge_git += 1
                    refmerge_increase.append((refmerge_conflicts - git_conflicts)/git_conflicts)


        all_intellimerge_reductions.extend(intellimerge_reductions)
        all_intellimerge_increase.extend(intellimerge_increase)
        all_refmerge_reductions.extend(refmerge_reductions)
        all_refmerge_increase.extend(refmerge_increase)

        if(len(intellimerge_reductions) > 0):
            median_intellimerge_reduction = statistics.median(intellimerge_reductions)

        if(len(intellimerge_increase) > 0):
            median_intellimerge_increase = statistics.median(intellimerge_increase)

        if(len(refmerge_reductions) > 0):
            median_refmerge_reduction = statistics.median(refmerge_reductions)

        if(len(refmerge_increase) > 0):
            median_refmerge_increase = statistics.median(refmerge_increase)

        #print("median reduction: " , statistics.median(intellimerge_reductions))
        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'total_resolvedIntelliMerge':total_resolved_intellimerge,
            'reduced_conflict_intellimerge': str(reduced_intellimerge_git) + " (" + "{:.0%}".format(median_intellimerge_reduction) + ")",
            'increased_conflict_intellimerge': str(increased_intellimerge_git) + " (" + "{:.0%}".format(median_intellimerge_increase) + ")",
            'same_conflict_intellimerge':same_intellimerge_git,
            'total_resolvedRefMerge':total_resolved_refmerge,
            'reduced_conflict_refmerge':str(reduced_refmerge_git) + " (" + "{:.0%}".format(median_refmerge_reduction) + ")",
            'increased_conflict_refmerge':str(increased_refmerge_git) + " (" + "{:.0%}".format(median_refmerge_increase) + ")",
            'same_conflict_refmerge':same_refmerge_git}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'total_resolvedIntelliMerge':all_intellimerge_resolved,
            'reduced_conflict_intellimerge': str(len(all_intellimerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intellimerge_reductions)) + ")",
            'increased_conflict_intellimerge': str(len(all_intellimerge_increase)) + " (" + "{:.0%}".format(statistics.median(all_intellimerge_increase)) + ")",
            'same_conflict_intellimerge':-1,
            'total_resolvedRefMerge':all_refmerge_resolved,
            'reduced_conflict_refmerge':str(len(all_refmerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refmerge_reductions)) + ")",
            'increased_conflict_refmerge':str(len(all_refmerge_increase)) + " (" + "{:.0%}".format(statistics.median(all_refmerge_increase)) + ")",
            'same_conflict_refmerge':-1}, ignore_index=True)
    df.to_csv("../results/overall.csv")

def get_stats_for_files_with_refactoring_conflicts():


def get_stats_for_supported_refactorings():
    merge_commits = get_supported_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue
            cb_group = cb_mc.get_group(mc_cb['id']) 

            if mc_has_supported_types(mc_cb['id']) == False:
                continue

            print(mc_cb['id'])

            refMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))
            intelliMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))
            git_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))

            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())



            refMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            intelliMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            git_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())


            print('Metric   ', 'RefMerge    ', 'IntelliMerge    ', 'Git ')
            print('Files:   ', refMerge_conflicting_files, "            ", intelliMerge_conflicting_files, "       ", git_conflicting_files)
            print('Blocks:  ', refMerge_conflict_blocks, "            ", intelliMerge_conflict_blocks, "       ", git_conflict_blocks)
            print('LOC:     ', refMerge_conflicting_loc, "            ", intelliMerge_conflicting_loc, "       ", git_conflicting_loc)




def get_data_frame(df_name):
    try:
        return pd.read_pickle(df_name + '.pickle')
    except FileNotFoundError:
        df = getattr(sys.modules[__name__], 'get_' + df_name)()
        df.to_pickle(df_name + '.pickle')
        return df

def print_stats():
    df = get_data_frame('conflict_block_stats')
    df = get_data_frame('conflicting_file_stats')
    df = get_data_frame('conflicting_stats')


if __name__ == '__main__':
    get_detailed_scenario_stats_per_project()
    get_detailed_file_stats_per_project()
    get_detailed_block_stats_per_project()
    get_detailed_loc_stats_per_project()
    #get_stats_for_supported_refactorings()
