import {TimeUtil} from './time-util';

describe('TimeUtil', () => {
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
