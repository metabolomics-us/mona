#!/usr/bin/env python

import argparse, sys


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description = '')
    parser.add_argument('filename', nargs = 1)
    parser.add_argument('-n', type = int)
    args = parser.parse_args()

    filename_output = args.filename[0].split('.')
    filename_output = '.'.join(filename_output[:-1]) + ('-dupe%d' % args.n) +'.'+ filename_output[-1]

    with open(args.filename[0], 'rU') as f, open(filename_output, 'w') as fout:
        data = f.read()
        fout.write('[\n' + ',\n'.join([data] * args.n) + '\n]')