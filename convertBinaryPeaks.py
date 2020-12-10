import zlib
from base64 import b64decode as b64dec
import array

import argparse

parser = argparse.ArgumentParser(description='ninary')

parser.add_argument("binary", help="echo the string you use here")
parser.add_argument("binaryIntensity", help="echo the string you use here")

args = parser.parse_args()

data = zlib.decompress(b64dec(args.binary))
doubles_sequence = array.array('d', data)

dataIntensity = zlib.decompress(b64dec(args.binaryIntensity))
doubles_sequenceIntensity = array.array('d', dataIntensity)

print(doubles_sequence.tolist())
print(doubles_sequenceIntensity.tolist())