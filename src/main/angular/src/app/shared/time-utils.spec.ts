import {TimeUtil} from './time-util';
import * as moment from 'moment';

describe('TimeUtil', () => {

  describe('toHourMinuteWithUnits', () => {
    it('display 0 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(0)).toEqual('0h 0min');
    });
    it('display 1 second', () => {
      expect(TimeUtil.toHourMinuteWithUnits(1)).toEqual('0h 1min');
    });
    it('display 60 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(1)).toEqual('0h 1min');
    });
    it('display 61 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(61)).toEqual('0h 2min');
    });
    it('display 3541 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(3541)).toEqual('1h 0min');
    });
    it('display 3600 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(3600)).toEqual('1h 0min');
    });
    it('display 86340 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(86340)).toEqual('23h 59min');
    });
    it('display 86341 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(86341)).toEqual('1d 0h 0min');
    });
    it('display 86400 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(86400)).toEqual('1d 0h 0min');
    });
    it('display 86401 seconds', () => {
      expect(TimeUtil.toHourMinuteWithUnits(86401)).toEqual('1d 0h 1min');
    });
  });

  describe('timestringOfNextMatchingDow', () => {
    let m = moment('2019-01-01T09:01:02.003Z');
    it('Should find a day in the same week', () => {
      expect(TimeUtil.timestringOfNextMatchingDow_(m, 5, '08:00'))
        .toEqual('2019-01-04T07:00:00.000Z');
    });
    it('Should find a day in the next week', () => {
      expect(TimeUtil.timestringOfNextMatchingDow_(m, 1, '08:00'))
        .toEqual('2019-01-07T07:00:00.000Z');
    });
    it('Should find a day in the next year', () => {
      m = moment('2019-12-31T09:01:02.003Z');
      expect(TimeUtil.timestringOfNextMatchingDow_(m, 1, '08:00'))
        .toEqual('2020-01-07T07:00:00.000Z');
    });
  });
});
